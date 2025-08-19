package me.jacksonhoggard.raydream.gui.font;

import imgui.ImFont;
import imgui.ImFontAtlas;
import me.jacksonhoggard.raydream.gui.config.LayoutConfig;
import me.jacksonhoggard.raydream.util.Util;

public class FontManager implements AutoCloseable {
    private ImFont titleFont;
    private ImFont bodyFont;
    private final float scale;
    
    public FontManager(float scale, ImFontAtlas fontAtlas) {
        this.scale = scale;
        loadFonts(fontAtlas);
    }
    
    private void loadFonts(ImFontAtlas fontAtlas) {
        fontAtlas.addFontDefault();
        bodyFont = fontAtlas.addFontFromMemoryTTF(
            Util.loadFont("WorkSans.ttf"), 
            LayoutConfig.BODY_FONT_SIZE * scale
        );
        titleFont = fontAtlas.addFontFromMemoryTTF(
            Util.loadFont("Inter.ttf"), 
            LayoutConfig.TITLE_FONT_SIZE * scale
        );
        fontAtlas.build();
    }
    
    public ImFont getTitleFont() {
        return titleFont;
    }
    
    public ImFont getBodyFont() {
        return bodyFont;
    }
    
    public float getScale() {
        return scale;
    }
    
    @Override
    public void close() {
        // ImGui handles font cleanup internally
    }
}
