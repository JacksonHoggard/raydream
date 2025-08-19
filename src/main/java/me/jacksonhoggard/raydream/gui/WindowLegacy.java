package me.jacksonhoggard.raydream.gui;

import imgui.ImFont;
import me.jacksonhoggard.raydream.core.ApplicationContext;

/**
 * Legacy Window class - now delegates to GUIApplication for backward compatibility
 * This maintains the existing API while using the new architecture underneath
 */
public class WindowLegacy {
    private final GUIApplication guiApplication;
    private static ImFont titleFont;
    private static ImFont bodyFont;
    
    public WindowLegacy(ApplicationContext context) {
        this.guiApplication = new GUIApplication(context);
    }
    
    public void init() {
        guiApplication.initialize();
        
        // Set static fonts for backward compatibility
        if (guiApplication.getImGuiSetup() != null) {
            titleFont = guiApplication.getImGuiSetup().getFontManager().getTitleFont();
            bodyFont = guiApplication.getImGuiSetup().getFontManager().getBodyFont();
        }
    }
    
    public void run() {
        guiApplication.run();
    }
    
    public void destroy() {
        guiApplication.close();
    }
    
    public void close() {
        guiApplication.shutdown();
    }
    
    public float getScale() {
        return guiApplication.getScale();
    }
    
    public static ImFont getTitleFont() {
        return titleFont;
    }
    
    public static ImFont getBodyFont() {
        return bodyFont;
    }
}
