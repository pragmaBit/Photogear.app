package app.photogear.util;

import java.io.InputStream;
import java.util.Properties;

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

    public static String get(String key) { return PROPS.getProperty(key); }
    public static String get(String key, String defaultValue) { return PROPS.getProperty(key, defaultValue); }
}
