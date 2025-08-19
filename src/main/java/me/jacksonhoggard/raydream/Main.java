package me.jacksonhoggard.raydream;

import me.jacksonhoggard.raydream.core.ApplicationContext;
import me.jacksonhoggard.raydream.gui.Window;
import me.jacksonhoggard.raydream.util.Logger;

public class Main {
    private static volatile boolean shutdownInProgress = false;
    
    public static void main(String[] args) {
        ApplicationContext context = ApplicationContext.getInstance();
        Logger logger = context.getLoggingService().getLogger(Main.class);
        Window window = null;

        logger.info("Starting RayDream application...");

        // Add shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!shutdownInProgress) {
                shutdownInProgress = true;
                logger.info("Shutdown hook triggered - performing graceful shutdown");
                try {
                    context.shutdown();
                } catch (Exception e) {
                    logger.error("Error during shutdown hook", e);
                }
            }
        }, "RayDream-Shutdown"));

        try {
            window = new Window(context);
            window.init();
            logger.info("Application initialized successfully");
            window.run();
        } catch (Exception e) {
            logger.error("Application error: " + e.getMessage(), e);
        } finally {
            shutdownInProgress = true;
            if (window != null) {
                try {
                    window.destroy();
                    logger.info("Window destroyed");
                } catch (Exception e) {
                    logger.error("Error destroying window", e);
                }
            }
            try {
                logger.info("Shutting down application...");
                context.shutdown();
            } catch (Exception e) {
                logger.error("Error during context shutdown", e);
            }
        }
        
        // Explicit exit to ensure clean shutdown
        logger.info("Application shutdown complete - exiting");
        System.exit(0);
    }
}