package me.jacksonhoggard.raydream.gui.imgui;

import imgui.ImDrawData;
import imgui.gl3.ImGuiImplGl3;

public class ImGuiImplGl3Wrapper implements AutoCloseable {
    private final ImGuiImplGl3 impl;
    
    public ImGuiImplGl3Wrapper() {
        this.impl = new ImGuiImplGl3();
    }
    
    public void init(String glslVersion) {
        impl.init(glslVersion);
    }
    
    public void renderDrawData(ImDrawData drawData) {
        impl.renderDrawData(drawData);
    }
    
    @Override
    public void close() {
        try {
            impl.dispose();
        } catch (Exception e) {
            // Log error but don't rethrow - we want cleanup to continue
            System.err.println("Error disposing ImGuiImplGl3: " + e.getMessage());
        }
    }
}
