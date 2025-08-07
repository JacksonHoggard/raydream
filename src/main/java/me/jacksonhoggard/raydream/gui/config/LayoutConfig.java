package me.jacksonhoggard.raydream.gui.config;

public class LayoutConfig {
    // Window size ratios
    public static final float EDITOR_WIDTH_RATIO = 0.5f;
    public static final float PROP_WINDOW_WIDTH_RATIO = 0.2f;
    public static final float OBJECT_WINDOW_HEIGHT_RATIO = 0.5f;
    public static final float PREVIEW_WINDOW_HEIGHT_RATIO = 0.5f;
    
    // Window constraints
    public static final int ASPECT_RATIO_WIDTH = 16;
    public static final int ASPECT_RATIO_HEIGHT = 9;
    public static final double MIN_WINDOW_SIZE_RATIO = 0.5;
    
    // UI spacing and sizing
    public static final float BUTTON_HEIGHT = 0.0f; // Let ImGui decide
    public static final float DEFAULT_ITEM_SPACING_X = 6.0f;
    public static final float DEFAULT_ITEM_SPACING_Y = 2.0f;
    public static final float DEFAULT_ITEM_INNER_SPACING_X = 8.0f;
    public static final float DEFAULT_ITEM_INNER_SPACING_Y = 4.0f;
    public static final float SCROLLBAR_SIZE = 12.0f;
    public static final float SCROLLBAR_ROUNDING = 16.0f;
    public static final float INDENT_SPACING = 100.0f;
    
    // Gizmo settings
    public static final float VIEW_MANIPULATE_SIZE_BASE = 128.0f;
    
    // Font sizes
    public static final float BODY_FONT_SIZE = 16.0f;
    public static final float TITLE_FONT_SIZE = 18.0f;
    
    // Render timing
    public static final double RENDER_INTERVAL = 0.016; // ~60 FPS
    
    // FPS display
    public static final double FPS_UPDATE_INTERVAL = 1.0;
    
    private LayoutConfig() {
        // Utility class
    }
}
