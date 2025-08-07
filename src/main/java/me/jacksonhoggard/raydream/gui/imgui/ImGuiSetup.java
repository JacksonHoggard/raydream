package me.jacksonhoggard.raydream.gui.imgui;

import imgui.*;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import me.jacksonhoggard.raydream.gui.config.LayoutConfig;
import me.jacksonhoggard.raydream.gui.font.FontManager;
import me.jacksonhoggard.raydream.util.Logger;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetWindowContentScale;

public class ImGuiSetup implements AutoCloseable {
    private final long windowPtr;
    private final Logger logger;
    private FontManager fontManager;
    private float scale;
    
    public ImGuiSetup(long windowPtr, Logger logger) {
        this.windowPtr = windowPtr;
        this.logger = logger;
    }
    
    public void initialize() {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        
        configureIO(io);
        calculateScale();
        setupFonts(io.getFonts());
        setupStyle();
        
        logger.info("ImGui initialized successfully with scale: " + scale);
    }
    
    private void configureIO(ImGuiIO io) {
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.setConfigViewportsNoTaskBarIcon(true);
    }
    
    private void calculateScale() {
        FloatBuffer scaleX = BufferUtils.createFloatBuffer(1);
        FloatBuffer scaleY = BufferUtils.createFloatBuffer(1);
        glfwGetWindowContentScale(windowPtr, scaleX, scaleY);
        scale = Math.max(scaleX.get(), scaleY.get());
    }
    
    private void setupFonts(ImFontAtlas fontAtlas) {
        fontManager = new FontManager(scale, fontAtlas);
    }
    
    private void setupStyle() {
        ImGuiStyle style = ImGui.getStyle();
        
        // Scale all sizes
        style.scaleAllSizes(scale);
        
        // Configure spacing and sizing
        style.setItemSpacing(LayoutConfig.DEFAULT_ITEM_SPACING_X, LayoutConfig.DEFAULT_ITEM_SPACING_Y);
        style.setItemInnerSpacing(LayoutConfig.DEFAULT_ITEM_INNER_SPACING_X, LayoutConfig.DEFAULT_ITEM_INNER_SPACING_Y);
        style.setIndentSpacing(LayoutConfig.INDENT_SPACING);
        style.setScrollbarSize(LayoutConfig.SCROLLBAR_SIZE);
        style.setScrollbarRounding(LayoutConfig.SCROLLBAR_ROUNDING);
        
        // Apply color theme
        applyColorTheme(style);
    }
    
    private void applyColorTheme(ImGuiStyle style) {
        // Dark theme colors
        style.setColor(ImGuiCol.Text, 0.86f, 0.93f, 0.89f, 0.78f);
        style.setColor(ImGuiCol.TextDisabled, 0.86f, 0.93f, 0.89f, 0.28f);
        style.setColor(ImGuiCol.WindowBg, 0.13f, 0.14f, 0.17f, 1.00f);
        style.setColor(ImGuiCol.Border, 0.31f, 0.31f, 1.00f, 0.00f);
        style.setColor(ImGuiCol.BorderShadow, 0.00f, 0.00f, 0.00f, 0.00f);
        style.setColor(ImGuiCol.FrameBg, 0.20f, 0.22f, 0.27f, 1.00f);
        style.setColor(ImGuiCol.FrameBgHovered, 0.92f, 0.18f, 0.29f, 0.78f);
        style.setColor(ImGuiCol.FrameBgActive, 0.92f, 0.18f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.TitleBg, 0.20f, 0.22f, 0.27f, 1.00f);
        style.setColor(ImGuiCol.TitleBgCollapsed, 0.20f, 0.22f, 0.27f, 0.75f);
        style.setColor(ImGuiCol.TitleBgActive, 0.92f, 0.18f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.MenuBarBg, 0.20f, 0.22f, 0.27f, 0.47f);
        style.setColor(ImGuiCol.ScrollbarBg, 0.20f, 0.22f, 0.27f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrab, 0.09f, 0.15f, 0.16f, 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, 0.92f, 0.18f, 0.29f, 0.78f);
        style.setColor(ImGuiCol.ScrollbarGrabActive, 0.92f, 0.18f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.CheckMark, 0.71f, 0.22f, 0.27f, 1.00f);
        style.setColor(ImGuiCol.SliderGrab, 0.47f, 0.77f, 0.83f, 0.14f);
        style.setColor(ImGuiCol.SliderGrabActive, 0.92f, 0.18f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.Button, 0.47f, 0.77f, 0.83f, 0.14f);
        style.setColor(ImGuiCol.ButtonHovered, 0.92f, 0.18f, 0.29f, 0.86f);
        style.setColor(ImGuiCol.ButtonActive, 0.92f, 0.18f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.Header, 0.92f, 0.18f, 0.29f, 0.76f);
        style.setColor(ImGuiCol.HeaderHovered, 0.92f, 0.18f, 0.29f, 0.86f);
        style.setColor(ImGuiCol.HeaderActive, 0.92f, 0.18f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.Separator, 0.20f, 0.22f, 0.27f, 1.0f);
        style.setColor(ImGuiCol.SeparatorHovered, 0.92f, 0.18f, 0.29f, 0.78f);
        style.setColor(ImGuiCol.SeparatorActive, 0.92f, 0.18f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.ResizeGrip, 0.47f, 0.77f, 0.83f, 0.04f);
        style.setColor(ImGuiCol.ResizeGripHovered, 0.92f, 0.18f, 0.29f, 0.78f);
        style.setColor(ImGuiCol.ResizeGripActive, 0.92f, 0.18f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.PlotLines, 0.86f, 0.93f, 0.89f, 0.63f);
        style.setColor(ImGuiCol.PlotLinesHovered, 0.92f, 0.18f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.PlotHistogram, 0.86f, 0.93f, 0.89f, 0.63f);
        style.setColor(ImGuiCol.PlotHistogramHovered, 0.92f, 0.18f, 0.29f, 1.00f);
        style.setColor(ImGuiCol.TextSelectedBg, 0.92f, 0.18f, 0.29f, 0.43f);
        style.setColor(ImGuiCol.PopupBg, 0.20f, 0.22f, 0.27f, 0.9f);
        style.setColor(ImGuiCol.ModalWindowDimBg, 0.20f, 0.22f, 0.27f, 0.73f);
    }
    
    public FontManager getFontManager() {
        return fontManager;
    }
    
    public float getScale() {
        return scale;
    }
    
    @Override
    public void close() {
        logger.info("Starting ImGui cleanup...");
        
        try {
            if (fontManager != null) {
                fontManager.close();
                fontManager = null;
                logger.debug("Font manager cleaned up");
            }
        } catch (Exception e) {
            logger.error("Error cleaning up font manager", e);
        }
        
        try {
            ImGui.destroyContext();
            logger.debug("ImGui context destroyed");
        } catch (Exception e) {
            logger.error("Error destroying ImGui context", e);
        }
        
        logger.info("ImGui cleanup complete");
    }
}
