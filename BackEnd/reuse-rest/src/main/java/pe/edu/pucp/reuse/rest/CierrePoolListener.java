package pe.edu.pucp.reuse.rest;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import pe.edu.pucp.reuse.db.DBManager;

/**
 * Cierra el pool de conexiones cuando GlassFish repliega la aplicacion; sin esto,
 * cada redespliegue dejaria abiertas las conexiones del pool anterior contra AWS.
 */
@WebListener
public class CierrePoolListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DBManager.cerrar();
    }
}
