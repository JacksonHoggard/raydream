package me.jacksonhoggard.raydream.gui.editor.window;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import me.jacksonhoggard.raydream.gui.MenuBar;
import me.jacksonhoggard.raydream.gui.editor.EditorCamera;
import me.jacksonhoggard.raydream.math.Vector3D;

public class SettingsWindow {

    private static float width;
    private static float height;
    private static float posX;
    private static float posY;
    private static final ImFloat inputFloat = new ImFloat();
    private static final ImInt inputInt = new ImInt();
    private static final float[] lookFrom = new float[] {0, 1, 2};
    private static final float[] lookAt = new float[] {0, 0, 0};
    private static final float[] up = new float[] {0, 1, 0};
    private static final float[] skyColor = new float[] {0, 0, 0, 1.F};
    private static float aperture = 100;
    private static int imgWidth = 1280;
    private static int imgHeight = 720;

    public static void show() {
        posX = EditorWindow.getPosX() + EditorWindow.getWidth();
        posY = MenuBar.getHeight() + EditorWindow.getHeight();
        width = ImGui.getMainViewport().getSizeX() - posX;
        height = ImGui.getMainViewport().getSizeY() - posY;
        ImGui.setNextWindowSize(width, height);
        ImGui.setNextWindowPos(posX, posY);
        ImGui.begin("Settings Window", ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoBringToFrontOnFocus);

        EditorCamera camera = PreviewWindow.getCamera();
        ImGui.inputFloat3("From", lookFrom);
        ImGui.inputFloat3("At", lookAt);
        ImGui.inputFloat3("Up", up);
        inputFloat.set(camera.getFov());
        ImGui.inputFloat("Field of View", inputFloat);
        camera.setFov(inputFloat.get());
        inputFloat.set(aperture);
        ImGui.inputFloat("Aperture", inputFloat);
        aperture = inputFloat.get();
        inputInt.set(imgWidth);
        ImGui.inputInt("Width", inputInt);
        imgWidth = inputInt.get();
        inputInt.set(imgHeight);
        ImGui.inputInt("Height", inputInt);
        imgHeight = inputInt.get();
        ImGui.inputFloat3("Sky Color", skyColor);

        camera.setAspect((float) imgWidth / (float) imgHeight);
        camera.updateProjection();
        camera.updateViewMatrix(
                new Vector3D(lookFrom[0], lookFrom[1], lookFrom[2]),
                new Vector3D(lookAt[0], lookAt[1], lookAt[2]),
                new Vector3D(up[0], up[1], up[2])
        );

        ImGui.end();
    }

    public static float getWidth() {
        return width;
    }

    public static float getHeight() {
        return height;
    }

    public static float getPosX() {
        return posX;
    }

    public static float getPosY() {
        return posY;
    }

    public static int getImgWidth() {
        return imgWidth;
    }

    public static int getImgHeight() {
        return imgHeight;
    }

    public static float getAperture() {
        return aperture;
    }

    public static float[] getLookAt() {
        return lookAt;
    }

    public static float[] getLookFrom() {
        return lookFrom;
    }

    public static float[] getUp() {
        return up;
    }

    public static float[] getSkyColor() {
        return skyColor;
    }
}
