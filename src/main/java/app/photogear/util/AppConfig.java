package app.photogear.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * Acceso centralizado a la configuración.
 *
 * Orden de prioridad (mayor a menor):
 *   1. Propiedad de sistema JVM  (-Dclave=valor)
 *   2. Variable de entorno       (CLAVE_EN_MAYUSCULAS, los '.' pasan a '_')
 *   3. config.properties en el classpath
 *
 * Esto permite mantener secretos (jwt.secret, contraseñas de BD, client id)
 * FUERA del WAR, inyectándolos por entorno en el despliegue.
 */
public class AppConfig {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream is = AppConfig.class.getResourceAsStream("/config.properties")) {
            if (is == null) throw new IllegalStateException("config.properties no encontrado en el classpath");
            PROPS.load(is);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys;

        String env = System.getenv(key.toUpperCase().replace('.', '_'));
        if (env != null && !env.isBlank()) return env;

        return PROPS.getProperty(key, defaultValue);
    }
}
