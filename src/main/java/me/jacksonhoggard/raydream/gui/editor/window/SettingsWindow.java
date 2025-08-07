package me.jacksonhoggard.raydream.gui.editor.window;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import me.jacksonhoggard.raydream.config.ApplicationConfig;
import me.jacksonhoggard.raydream.core.ApplicationContext;
import me.jacksonhoggard.raydream.gui.MenuBar;
import me.jacksonhoggard.raydream.gui.Window;
import me.jacksonhoggard.raydream.gui.editor.EditorCamera;
import me.jacksonhoggard.raydream.light.PointLight;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.render.Scene;
import me.jacksonhoggard.raydream.service.SceneService;
import me.jacksonhoggard.raydream.util.Logger;

import java.io.IOException;

import java.nio.file.Path;

public class SettingsWindow {
    private static final Logger logger = ApplicationContext.getInstance().getLoggingService().getLogger(SettingsWindow.class);

    private static float width;
    private static float height;
    private static float posX;
    private static float posY;
    private static final ImFloat inputFloat = new ImFloat();
    private static final ImInt inputInt = new ImInt();
    private static float[] lookFrom = new float[] {0, 1, 2};
    private static float[] lookAt = new float[] {0, 0, 0};
    private static float[] up = new float[] {0, 1, 0};
    private static float[] skyColor = new float[] {0, 0, 0, 1.f};
    private static float[] ambientColor = new float[] {1.f, 1.f, 1.f};
    private static float aperture = 100;
    private static int imgWidth = ApplicationConfig.DEFAULT_WINDOW_WIDTH;
    private static int imgHeight = ApplicationConfig.DEFAULT_WINDOW_HEIGHT;
    private static int sampleDepth = 2;
    private static int bounces = ApplicationConfig.DEFAULT_MAX_BOUNCE_DEPTH;
    private static int numShadowRays = 8;
    private static int threads = ApplicationConfig.DEFAULT_THREAD_COUNT;
    private static final SceneService sceneService = ApplicationContext.getInstance().getSceneService();

    public static void show() {
        posX = EditorWindow.getPosX() + EditorWindow.getWidth();
        posY = MenuBar.getHeight() + EditorWindow.getHeight();
        width = ImGui.getMainViewport().getSizeX() - posX;
        height = ImGui.getMainViewport().getSizeY() - posY;
        ImGui.setNextWindowSize(width, height);
        ImGui.setNextWindowPos(posX, posY);
        ImGui.pushFont(Window.getBodyFont());
        ImGui.begin("Settings Window", ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.AlwaysVerticalScrollbar | ImGuiWindowFlags.AlwaysHorizontalScrollbar);

        ImGui.pushFont(Window.getTitleFont());
        ImGui.text("Camera Settings");
        ImGui.popFont();

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
        ImGui.colorEdit3("Sky Color", skyColor);
        ImGui.colorEdit3("Ambient Color", ambientColor);

        camera.setAspect((float) imgWidth / (float) imgHeight);
        camera.setLookFrom(lookFrom[0], lookFrom[1], lookFrom[2]);
        camera.setLookAt(lookAt[0], lookAt[1], lookAt[2]);
        camera.setUp(up[0], up[1], up[2]);
        camera.updateProjection();
        camera.updateViewMatrix();
        camera.updateModelMatrix();

        ImGui.separator();

        ImGui.pushFont(Window.getTitleFont());
        ImGui.text("Render Settings");
        ImGui.popFont();

        inputInt.set(sampleDepth);
        ImGui.inputInt("Samples per Pixel", inputInt);
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
            String path = DialogWindow.openFileSave("output.png", "png", "jpg");
            if(path != null) {
                if(!(path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".jpeg")))
                    path += ".png";
                DialogWindow.showProgressBar("Render Progress", 250, 100, Scene.getRenderCancelListener());
                try {
                    sceneService.renderScene(
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
                } catch (IOException e) {
                    logger.error("Failed to render scene", e);
                    // TODO: Show error dialog to user
                }
                if(!Scene.getRenderCancelListener().isCanceled())
                    DialogWindow.openImage(Path.of(path).getFileName().toString(), path, imgWidth, imgHeight);
            }
        }

        ImGui.popFont();
        ImGui.end();
    }

    public static void reset() {
        lookFrom = new float[] {0, 1, 2};
        lookAt = new float[] {0, 0, 0};
        up = new float[] {0, 1, 0};
        skyColor = new float[] {0, 0, 0, 1.f};
        ambientColor = new float[] {1.f, 1.f, 1.f};
        aperture = 100;
        imgWidth = ApplicationConfig.DEFAULT_WINDOW_WIDTH;
        imgHeight = ApplicationConfig.DEFAULT_WINDOW_HEIGHT;
        sampleDepth = 2;
        bounces = ApplicationConfig.DEFAULT_MAX_BOUNCE_DEPTH;
        numShadowRays = 8;
        threads = ApplicationConfig.DEFAULT_THREAD_COUNT;
    }

    public static String toSaveEntry() {
        return "+ settings:\n" +
                "lookFrom: " + lookFrom[0] + " " + lookFrom[1] + " " + lookFrom[2] + "\n" +
                "lookAt: " + lookAt[0] + " " + lookAt[1] + " " + lookAt[2] + "\n" +
                "up: " + up[0] + " " + up[1] + " " + up[2] + "\n" +
                "sky: " + skyColor[0] + " " + skyColor[1] + " " + skyColor[2] + "\n" +
                "ambient: " + ambientColor[0] + " " + ambientColor[1] + " " + ambientColor[2] + "\n" +
                "fov: " + PreviewWindow.getCamera().getFov() + "\n" +
                "aperture: " + aperture + "\n" +
                "width: " + imgWidth + "\n" +
                "height: " + imgHeight + "\n" +
                "samples: " + sampleDepth + "\n" +
                "bounces: " + bounces + "\n" +
                "shadows: " + numShadowRays + "\n" +
                "threads: " + threads + "\n" +
                ";\n";
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

    public static void setImgWidth(int imgWidth) {
        SettingsWindow.imgWidth = imgWidth;
    }

    public static void setImgHeight(int imgHeight) {
        SettingsWindow.imgHeight = imgHeight;
    }

    public static void setAperture(float aperture) {
        SettingsWindow.aperture = aperture;
    }

    public static void setSkyColor(float[] skyColor) {
        SettingsWindow.skyColor = new float[] {
                skyColor[0],
                skyColor[1],
                skyColor[2],
                1.0f
        };
    }

    public static void setAmbientColor(float[] ambientColor) {
        SettingsWindow.ambientColor = ambientColor;
    }

    public static void setBounces(int bounces) {
        SettingsWindow.bounces = bounces;
    }

    public static void setLookAt(float[] lookAt) {
        SettingsWindow.lookAt = lookAt;
    }

    public static void setLookFrom(float[] lookFrom) {
        SettingsWindow.lookFrom = lookFrom;
    }

    public static void setNumShadowRays(int numShadowRays) {
        SettingsWindow.numShadowRays = numShadowRays;
    }

    public static void setSampleDepth(int sampleDepth) {
        SettingsWindow.sampleDepth = sampleDepth;
    }

    public static void setUp(float[] up) {
        SettingsWindow.up = up;
    }

    public static void setThreads(int threads) {
        SettingsWindow.threads = threads;
    }

    public static void setFov(float fov) {
        PreviewWindow.getCamera().setFov(fov);
    }
}
