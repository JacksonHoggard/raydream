package me.jacksonhoggard.raydream.gui.imgui;

import imgui.glfw.ImGuiImplGlfw;

public class ImGuiImplGlfwWrapper implements AutoCloseable {
    private final ImGuiImplGlfw impl;
    
    public ImGuiImplGlfwWrapper() {
        this.impl = new ImGuiImplGlfw();
    }
    
    public void init(long windowPtr, boolean installCallbacks) {
        impl.init(windowPtr, installCallbacks);
    }
    
    public void newFrame() {
        impl.newFrame();
    }
    
    @Override
    public void close() {
        try {
            impl.dispose();
        } catch (Exception e) {
            // Log error but don't rethrow - we want cleanup to continue
            System.err.println("Error disposing ImGuiImplGlfw: " + e.getMessage());
        }
    }
}
