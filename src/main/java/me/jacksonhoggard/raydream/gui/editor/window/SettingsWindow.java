package me.jacksonhoggard.raydream.gui.editor.window;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import me.jacksonhoggard.raydream.SceneManager;
import me.jacksonhoggard.raydream.gui.MenuBar;
import me.jacksonhoggard.raydream.gui.editor.EditorCamera;
import me.jacksonhoggard.raydream.light.PointLight;
import me.jacksonhoggard.raydream.math.Vector3D;

import java.nio.file.Path;

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
    private static final float[] skyColor = new float[] {0, 0, 0, 1.f};
    private static final float[] ambientColor = new float[] {1.f, 1.f, 1.f};
    private static float aperture = 100;
    private static int imgWidth = 1280;
    private static int imgHeight = 720;
    private static int sampleDepth = 2;
    private static int bounces = 8;
    private static int numShadowRays = 8;
    private static int threads = 4;

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
        ImGui.inputFloat3("To", lookAt);
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
        ImGui.inputFloat3("Ambient Color", ambientColor);

        camera.setAspect((float) imgWidth / (float) imgHeight);
        camera.setLookFrom(lookFrom[0], lookFrom[1], lookFrom[2]);
        camera.setLookAt(lookAt[0], lookAt[1], lookAt[2]);
        camera.setUp(up[0], up[1], up[2]);
        camera.updateProjection();
        camera.updateViewMatrix();
        camera.updateModelMatrix();

        ImGui.separator();
        inputInt.set(sampleDepth);
        ImGui.inputInt("Sample Depth", inputInt);
        sampleDepth = inputInt.get();
        inputInt.set(bounces);
        ImGui.inputInt("Bounces", inputInt);
        bounces = inputInt.get();
        inputInt.set(numShadowRays);
        ImGui.inputInt("# of Shadow Rays", inputInt);
        numShadowRays = inputInt.get();
        inputInt.set(threads);
        ImGui.inputInt("# of Threads", inputInt);
        threads = inputInt.get();
        if(ImGui.button("Render")) {
            String path = DialogWindow.openFileSave("output.png");
            if(path != null) {
                DialogWindow.showProgressBar("Render Progress", 250, 100);
                SceneManager.renderScene(
                        ObjectWindow.objects,
                        ObjectWindow.lights,
                        new PointLight(
                                new Vector3D(),
                                new Vector3D(
                                        ambientColor[0],
                                        ambientColor[1],
                                        ambientColor[2]
                                ),
                                1
                        ),
                        new Vector3D(
                                skyColor[0],
                                skyColor[1],
                                skyColor[2]
                        ),
                        camera,
                        imgWidth,
                        imgHeight,
                        aperture,
                        path,
                        sampleDepth,
                        bounces,
                        numShadowRays,
                        threads,
                        DialogWindow.getProgressListener()
                );
                DialogWindow.openImage(Path.of(path).getFileName().toString(), path, imgWidth, imgHeight);
            }
        }

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

    public static float[] getSkyColor() {
        return skyColor;
    }

    public static float[] getAmbientColor() {
        return ambientColor;
    }
}
