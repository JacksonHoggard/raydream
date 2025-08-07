package me.jacksonhoggard.raydream.gui.error;

import me.jacksonhoggard.raydream.gui.editor.window.DialogWindow;
import me.jacksonhoggard.raydream.util.Logger;

public class GUIErrorHandler {
    private final Logger logger;
    
    public GUIErrorHandler(Logger logger) {
        this.logger = logger;
    }
    
    public void handleError(String context, Exception e) {
        logger.error(context + ": " + e.getMessage(), e);
        DialogWindow.showError(context, e);
    }
    
    public void handleError(String context, String message) {
        logger.error(context + ": " + message);
        DialogWindow.showError(context, new Exception(message));
    }
    
    public void handleWarning(String context, String message) {
        logger.warn(context + ": " + message);
        // Could show a warning dialog if needed
    }
    
    public void handleCriticalError(String context, Exception e) {
        logger.error("CRITICAL ERROR - " + context + ": " + e.getMessage(), e);
        DialogWindow.showError("Critical Error: " + context, e);
        // Could trigger application shutdown if needed
    }
}
