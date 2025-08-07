package me.jacksonhoggard.raydream.gui;

import me.jacksonhoggard.raydream.core.ApplicationContext;
import me.jacksonhoggard.raydream.util.Logger;

/**
 * Test class to verify clean startup and shutdown
 */
public class GUITest {
    public static void main(String[] args) {
        ApplicationContext context = ApplicationContext.getInstance();
        Logger logger = context.getLoggingService().getLogger(GUITest.class);
        
        logger.info("=== Starting GUI Test ===");
        
        try {
            GUIApplication app = new GUIApplication(context);
            logger.info("GUIApplication created successfully");
            
            app.initialize();
            logger.info("GUIApplication initialized successfully");
            
            // Don't run the main loop - just test initialization and cleanup
            Thread.sleep(1000); // Give it a moment
            
            app.close();
            logger.info("GUIApplication closed successfully");
            
        } catch (Exception e) {
            logger.error("Error during GUI test", e);
            e.printStackTrace();
        } finally {
            context.shutdown();
            logger.info("=== GUI Test Complete ===");
        }
    }
}
