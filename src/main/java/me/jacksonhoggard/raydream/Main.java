package me.jacksonhoggard.raydream;

import me.jacksonhoggard.raydream.core.ApplicationContext;
import me.jacksonhoggard.raydream.gui.Window;
import me.jacksonhoggard.raydream.util.Logger;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = ApplicationContext.getInstance();
        Logger logger = context.getLoggingService().getLogger(Main.class);
        Window window = null;

        logger.info("Starting RayDream application...");

        try {
            window = new Window(context);
            window.init();
            logger.info("Application initialized successfully");
            window.run();
        } catch (Exception e) {
            logger.error("Application error: " + e.getMessage(), e);
        } finally {
            if (window != null) {
                window.destroy();
                logger.info("Window destroyed");
            }
            logger.info("Shutting down application...");
            context.shutdown();
        }
    }
}