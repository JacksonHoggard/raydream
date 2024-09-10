package me.jacksonhoggard.raydream.gui.editor.window;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import me.jacksonhoggard.raydream.gui.editor.EditorCamera;
import me.jacksonhoggard.raydream.render.FrameBuffer;

public class PreviewWindow {

    private static EditorCamera camera = new EditorCamera(60, 16/9.F, 0.0001F, 1000F);
    private static float posX;
    private static float posY;
    private static float width;
    private static float height;
    private static float frameWidth;
    private static float frameHeight;
    private static float framePosX;
    private static float framePosY;

    public static void show(FrameBuffer frameBuffer) {
        posX = EditorWindow.getPosX();
        posY = EditorWindow.getPosY() + EditorWindow.getHeight();
        width = EditorWindow.getWidth();
        height = EditorWindow.getHeight();
        ImGui.setNextWindowPos(posX, posY, ImGuiCond.Always);
        ImGui.setNextWindowSize(width, height, ImGuiCond.Always);
        ImGui.begin("Scene Preview", new ImBoolean(true), ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoBringToFrontOnFocus);
        frameWidth = SettingsWindow.getImgWidth() > SettingsWindow.getImgHeight() ? width : height * camera.getAspect();
        frameHeight = SettingsWindow.getHeight() > SettingsWindow.getImgWidth() ? height : width / camera.getAspect();
        while(frameWidth > width)
            frameWidth--;
        frameHeight = frameWidth / camera.getAspect();
        while(frameHeight > height)
            frameHeight--;
        frameWidth = frameHeight * camera.getAspect();
        framePosX = posX + (0.5F * width) - (0.5F * frameWidth);
        framePosY = posY + (0.5F * height) - (0.5F * frameHeight);
        frameBuffer.setClearColor(SettingsWindow.getSkyColor());
        frameBuffer.rescale((int) frameWidth, (int) frameHeight);
        ImGui.getWindowDrawList().addImage(
                frameBuffer.getTextureId(),
                framePosX, framePosY,
                framePosX + frameWidth, framePosY + frameHeight,
                0, 1,
                1, 0
        );
        ImGui.end();
    }

    public static EditorCamera getCamera() {
        return camera;
    }

    public static float getFrameWidth() {
        return frameWidth;
    }

    public static float getFrameHeight() {
        return frameHeight;
    }

    public static float getWidth() {
        return width;
    }

    public static float getHeight() {
        return height;
    }
}
