package com.giftora.listener;

import com.giftora.dao.Database;
import com.giftora.dao.DatabaseInitializer;
import com.giftora.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Initializes the database (schema + seed) and the chatbot on application startup,
 * and closes the connection pool on shutdown.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(AppContextListener.class);
    public static final String CHATBOT_ATTR = "chatbotService";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            DatabaseInitializer.initialize();
            ChatbotService chatbotService = new ChatbotService();
            ServletContext context = sce.getServletContext();
            context.setAttribute(CHATBOT_ATTR, chatbotService);
            log.info("Giftora started. Chatbot provider: {}", chatbotService.providerName());
        } catch (Exception ex) {
            log.error("Application startup failed", ex);
            throw new IllegalStateException("Unable to start Giftora", ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Database.getInstance().close();
        log.info("Giftora stopped and database pool closed");
    }
}
