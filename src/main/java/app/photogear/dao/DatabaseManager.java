package app.photogear.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona el pool de conexiones HikariCP con soporte para dos perfiles:
 *  - h2       → desarrollo / pruebas  (base de datos embebida)
 *  - mariadb  → producción
 *
 * Selección de perfil (en orden de prioridad):
 *  1. Propiedad JVM:  -Ddb.profile=mariadb
 *  2. config.properties → db.profile
 *  3. Default: h2
 */
public class DatabaseManager {

    private static final Logger LOG = Logger.getLogger(DatabaseManager.class.getName());

    private static final String CONFIG_RESOURCE = "/config.properties";
    private static final String SCHEMA_RESOURCE = "/schema.sql";

    private static HikariDataSource dataSource;
    private static String activeProfile;

    static {
        try (InputStream is = DatabaseManager.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (is == null) {
                throw new IllegalStateException("No se encontró config.properties en el classpath");
            }
            Properties props = new Properties();
            props.load(is);

            activeProfile = System.getProperty("db.profile",
                            props.getProperty("db.profile", "h2")).trim();

            String driver   = props.getProperty(activeProfile + ".driver");
            String url      = props.getProperty(activeProfile + ".url");
            String username = props.getProperty(activeProfile + ".username");
            String password = props.getProperty(activeProfile + ".password", "");

            if (driver == null || url == null) {
                throw new IllegalStateException(
                    "Configuración incompleta para el perfil: " + activeProfile);
            }

            HikariConfig config = new HikariConfig();
            config.setDriverClassName(driver);
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setMinimumIdle(Integer.parseInt(
                props.getProperty("hikari.minimum-idle", "2")));
            config.setMaximumPoolSize(Integer.parseInt(
                props.getProperty("hikari.maximum-pool-size", "10")));
            config.setConnectionTimeout(Long.parseLong(
                props.getProperty("hikari.connection-timeout", "30000")));
            config.setIdleTimeout(Long.parseLong(
                props.getProperty("hikari.idle-timeout", "600000")));
            config.setMaxLifetime(Long.parseLong(
                props.getProperty("hikari.max-lifetime", "1800000")));
            config.setPoolName("PhotogearPool");

            dataSource = new HikariDataSource(config);
            LOG.info("Pool HikariCP inicializado. Perfil: " + activeProfile + " | URL: " + url);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error fatal inicializando DatabaseManager", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    /** Retorna una conexión del pool. El llamador debe cerrarla (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Inicializa el esquema ejecutando schema.sql.
     * Seguro de invocar múltiples veces gracias a IF NOT EXISTS.
     */
    public static void initSchema() {
        LOG.info("Inicializando esquema de base de datos...");
        try (InputStream is = DatabaseManager.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (is == null) {
                LOG.warning("schema.sql no encontrado en el classpath. Omitiendo inicialización.");
                return;
            }

            String fullSql = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            try (Connection conn = getConnection();
                 Statement  stmt = conn.createStatement()) {

                for (String raw : fullSql.split(";")) {
                    String sql = raw.replaceAll("--[^\n]*", "").trim();
                    if (!sql.isEmpty()) {
                        stmt.execute(sql);
                    }
                }
                LOG.info("Esquema inicializado correctamente.");
            }

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error inicializando el esquema", e);
            throw new RuntimeException("No se pudo inicializar el esquema de BD", e);
        }
    }

    /** Cierra el pool al detener la aplicación. */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOG.info("Pool de conexiones cerrado.");
        }
    }

    public static String getActiveProfile() { return activeProfile; }

    public static boolean isH2() { return "h2".equalsIgnoreCase(activeProfile); }
}
