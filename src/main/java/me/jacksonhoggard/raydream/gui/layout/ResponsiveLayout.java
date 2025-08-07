package me.jacksonhoggard.raydream.gui.layout;

import me.jacksonhoggard.raydream.gui.config.LayoutConfig;

public class ResponsiveLayout {
    
    public static class WindowBounds {
        public final float x, y, width, height;
        
        public WindowBounds(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
    
    private final float viewportWidth;
    private final float viewportHeight;
    private final float menuBarHeight;
    
    public ResponsiveLayout(float viewportWidth, float viewportHeight, float menuBarHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.menuBarHeight = menuBarHeight;
    }
    
    public WindowBounds calculatePropWindowBounds() {
        float width = viewportWidth * LayoutConfig.PROP_WINDOW_WIDTH_RATIO;
        float height = viewportHeight - menuBarHeight;
        return new WindowBounds(0, menuBarHeight, width, height);
    }
    
    public WindowBounds calculateEditorWindowBounds() {
        WindowBounds propBounds = calculatePropWindowBounds();
        float width = viewportWidth * LayoutConfig.EDITOR_WIDTH_RATIO;
        float height = (viewportHeight - menuBarHeight) * LayoutConfig.OBJECT_WINDOW_HEIGHT_RATIO;
        return new WindowBounds(propBounds.width, menuBarHeight, width, height);
    }
    
    public WindowBounds calculateObjectWindowBounds() {
        WindowBounds propBounds = calculatePropWindowBounds();
        WindowBounds editorBounds = calculateEditorWindowBounds();
        float width = viewportWidth - (propBounds.width + editorBounds.width);
        float height = (viewportHeight - menuBarHeight) * LayoutConfig.OBJECT_WINDOW_HEIGHT_RATIO;
        float x = propBounds.width + editorBounds.width;
        return new WindowBounds(x, menuBarHeight, width, height);
    }
    
    public WindowBounds calculatePreviewWindowBounds() {
        WindowBounds editorBounds = calculateEditorWindowBounds();
        float width = editorBounds.width;
        float height = (viewportHeight - menuBarHeight) * LayoutConfig.PREVIEW_WINDOW_HEIGHT_RATIO;
        float y = editorBounds.y + editorBounds.height;
        return new WindowBounds(editorBounds.x, y, width, height);
    }
    
    public WindowBounds calculateSettingsWindowBounds() {
        WindowBounds objectBounds = calculateObjectWindowBounds();
        float width = objectBounds.width;
        float height = (viewportHeight - menuBarHeight) * LayoutConfig.PREVIEW_WINDOW_HEIGHT_RATIO;
        float y = objectBounds.y + objectBounds.height;
        return new WindowBounds(objectBounds.x, y, width, height);
    }
}
