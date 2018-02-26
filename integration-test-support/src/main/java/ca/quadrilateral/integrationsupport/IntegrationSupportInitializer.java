package ca.quadrilateral.integrationsupport;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ApplicationPath("/integration")
@WebListener
public class IntegrationSupportInitializer extends Application implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationSupportInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing Integration Test Support...");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Terminating Integration Test Support...");
    }
}
