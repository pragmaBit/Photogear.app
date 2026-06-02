package app.photogear.listener;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import app.photogear.dao.DatabaseManager;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Se ejecuta automáticamente cuando Tomcat despliega la aplicación.
 * Inicializa el esquema de base de datos (CREATE TABLE IF NOT EXISTS).
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(AppContextListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info("╔═══════════════════════════════════════╗");
        LOG.info("║     photogear.app – Iniciando aplicación   ║");
        LOG.info("╚═══════════════════════════════════════╝");
        LOG.info("Perfil de base de datos activo: " + DatabaseManager.getActiveProfile());

        try {
            DatabaseManager.initSchema();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error al inicializar el esquema. La aplicación puede no funcionar.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DatabaseManager.close();
        LOG.info("photogear.app – Aplicación detenida.");
    }
}
