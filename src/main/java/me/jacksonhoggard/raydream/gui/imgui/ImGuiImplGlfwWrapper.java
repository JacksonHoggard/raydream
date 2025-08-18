package me.jacksonhoggard.raydream.gui.imgui;

import imgui.glfw.ImGuiImplGlfw;

public class ImGuiImplGlfwWrapper implements AutoCloseable {
    private final ImGuiImplGlfw impl;
    private long windowPtr;
    
    public ImGuiImplGlfwWrapper() {
        this.impl = new ImGuiImplGlfw();
    }
    
    public void init(long windowPtr, boolean installCallbacks) {
        this.windowPtr = windowPtr;
        impl.init(windowPtr, installCallbacks);
    }
    
    public void newFrame() {
        impl.newFrame();
    }
    
    // Manual input handling methods for when installCallbacks is false
    public void mouseButtonCallback(int button, int action, int mods) {
        impl.mouseButtonCallback(windowPtr, button, action, mods);
    }
    
    public void scrollCallback(double dx, double dy) {
        impl.scrollCallback(windowPtr, dx, dy);
    }
    
    public void keyCallback(int key, int scancode, int action, int mods) {
        impl.keyCallback(windowPtr, key, scancode, action, mods);
    }
    
    public void charCallback(int c) {
        impl.charCallback(windowPtr, c);
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
