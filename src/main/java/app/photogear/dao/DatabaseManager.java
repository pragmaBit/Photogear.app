package app.photogear.dao;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestiona la conexión JDBC con soporte para dos perfiles:
 *  - h2       → desarrollo / pruebas  (base de datos embebida, sin instalación)
 *  - mariadb  → producción
 *
 * Selección de perfil (en orden de prioridad):
 *  1. Propiedad JVM:  -Ddb.profile=mariadb
 *  2. config.properties → db.profile
 *  3. Default: h2
 *
 * Para cambiar en Tomcat, agrega a $CATALINA_HOME/bin/setenv.sh:
 *     JAVA_OPTS="$JAVA_OPTS -Ddb.profile=mariadb"
 */
public class DatabaseManager {

    private static final Logger LOG = Logger.getLogger(DatabaseManager.class.getName());

    private static final String CONFIG_RESOURCE = "/config.properties";
    private static final String SCHEMA_RESOURCE = "/schema.sql";

    private static String url;
    private static String username;
    private static String password;
    private static String activeProfile;

    static {
        try (InputStream is = DatabaseManager.class.getResourceAsStream(CONFIG_RESOURCE)) {
            if (is == null) {
                throw new IllegalStateException("No se encontró config.properties en el classpath");
            }
            Properties props = new Properties();
            props.load(is);

            // Propiedad JVM tiene prioridad sobre config.properties
            activeProfile = System.getProperty("db.profile",
                            props.getProperty("db.profile", "h2")).trim();

            String driver = props.getProperty(activeProfile + ".driver");
            url      = props.getProperty(activeProfile + ".url");
            username = props.getProperty(activeProfile + ".username");
            password = props.getProperty(activeProfile + ".password", "");

            if (driver == null || url == null) {
                throw new IllegalStateException(
                    "Configuración incompleta para el perfil: " + activeProfile);
            }

            Class.forName(driver);
            LOG.info("DatabaseManager inicializado. Perfil: " + activeProfile + " | URL: " + url);

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error fatal inicializando DatabaseManager", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Retorna una nueva conexión JDBC.
     * El llamador es responsable de cerrarla (usar try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Inicializa el esquema de base de datos ejecutando schema.sql.
     * Se llama desde AppContextListener al arrancar Tomcat.
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

                // Dividir en sentencias individuales (ignorar comentarios y líneas vacías)
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

    /** Retorna el nombre del perfil activo ('h2' o 'mariadb'). */
    public static String getActiveProfile() {
        return activeProfile;
    }

    /** Indica si se está usando H2 (útil para adaptaciones de SQL). */
    public static boolean isH2() {
        return "h2".equalsIgnoreCase(activeProfile);
    }
}
