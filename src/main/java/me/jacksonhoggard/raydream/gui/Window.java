package me.jacksonhoggard.raydream.gui;

import imgui.*;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import me.jacksonhoggard.raydream.config.ApplicationConfig;
import me.jacksonhoggard.raydream.core.ApplicationContext;
import me.jacksonhoggard.raydream.gui.editor.EditorCamera;
import me.jacksonhoggard.raydream.gui.editor.light.EditorAreaLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorPointLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorSphereLight;
import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.MeshModel;
import me.jacksonhoggard.raydream.gui.editor.object.*;
import me.jacksonhoggard.raydream.gui.editor.window.*;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.*;
import me.jacksonhoggard.raydream.render.FrameBuffer;
import me.jacksonhoggard.raydream.render.Shader;
import me.jacksonhoggard.raydream.util.Util;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final ApplicationContext context;
    private final ImGuiImplGlfw imGuiGlfw;
    private final ImGuiImplGl3 imGuiGl3;
    private Shader objectShader;
    private Shader lightShader;
    private FrameBuffer editorFrameBuffer;
    private FrameBuffer previewFrameBuffer;
    private float scale;
    private static ImFont titleFont;
    private static ImFont bodyFont;

    private double fpsTimer;
    private int fps;

    private String glslVersion = null;
    private long windowPtr;

    private boolean isMiddleMouseButtonPressed = false;

    private final GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
        @Override
        public void invoke(long window, double dx, double dy) {
            EditorWindow.setCamDistance(EditorWindow.getCamDistance() + ((float) -dy * ApplicationConfig.ZOOM_STEP));
        }
    };

    private final GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            if(button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                switch(action) {
                    case GLFW_PRESS -> isMiddleMouseButtonPressed = true;
                    case GLFW_RELEASE -> isMiddleMouseButtonPressed = false;
                }
            }
        }
    };

    private final GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
        private double lastX = 0;
        private double lastY = 0;
        private boolean firstCall = true;

        @Override
        public void invoke(long window, double x, double y) {
            if(firstCall) {
                lastX = x;
                lastY = y;
                firstCall = false;
            }
            double deltaX = -(x - lastX);
            double deltaY = y - lastY;

            if(isMiddleMouseButtonPressed)
                EditorWindow.cursorMoveCamera((float) (deltaX * ApplicationConfig.CAMERA_MOVE_DELTA),
                                            (float) (deltaY * ApplicationConfig.CAMERA_MOVE_DELTA));

            lastX = x;
            lastY = y;
        }
    };

    public Window(ApplicationContext context) {
        this.context = context;
        this.imGuiGlfw = new ImGuiImplGlfw();
        this.imGuiGl3 = new ImGuiImplGl3();
    }

    public void init() {
        initWindow();
        initImGui();
        imGuiGlfw.init(windowPtr, true);
        imGuiGl3.init(glslVersion);
        objectShader = new Shader("object_vertex_shader.glsl", "object_fragment_shader.glsl");
        lightShader = new Shader("light_vertex_shader.glsl", "light_fragment_shader.glsl");
        editorFrameBuffer = new FrameBuffer(1920, 1080);
        previewFrameBuffer = new FrameBuffer(1920, 1080);
        fps = 0;
    }

    public void destroy() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
        Callbacks.glfwFreeCallbacks(windowPtr);
        objectShader.cleanup();
        lightShader.cleanup();
        editorFrameBuffer.cleanup();
        previewFrameBuffer.cleanup();
        glfwDestroyWindow(windowPtr);
        glfwTerminate();
        for(EditorObject object : ObjectWindow.objects) {
            object.remove();
        }
        EditorAreaLight.cleanup();
        EditorPointLight.cleanup();
        EditorSphereLight.cleanup();
        BoxEditorObject.cleanup();
        PlaneEditorObject.cleanup();
        SphereEditorObject.cleanup();
        EditorCamera.getModel().remove();
    }

    private void initWindow() {
        // Initialize GLFW
        GLFWErrorCallback.createPrint(System.err).set();

        if(!glfwInit()) {
            System.out.println("Unable to initialize GLFW");
            System.exit(-1);
        }

        // Initialize GLSL
        glslVersion = "#version 330";
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);

        // Create window
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        windowPtr = glfwCreateWindow((int) (vidMode.width() * 0.8D), (int) (vidMode.height() * 0.8D), "RayDream", NULL, NULL);

        if(windowPtr == NULL) {
            System.out.println("Unable to create window");
            System.exit(-1);
        }

        // Set window icon
        ByteBuffer iconBuffer;
        ByteBuffer decodedImage;
        int width, height;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            String iconPath = "logo.png";
            InputStream iconStream = ClassLoader.getSystemResourceAsStream(iconPath);
            if (iconStream == null) {
                throw new RuntimeException("Icon file not found: " + iconPath);
            }

            iconBuffer = ByteBuffer.allocateDirect(iconStream.available());
            Channels.newChannel(iconStream).read(iconBuffer);
            iconBuffer.flip();

            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelsBuffer = stack.mallocInt(1);
            decodedImage = STBImage.stbi_load_from_memory(iconBuffer, widthBuffer, heightBuffer, channelsBuffer, 4);
            if (decodedImage == null) {
                throw new RuntimeException("Failed to load icon: " + STBImage.stbi_failure_reason());
            }

            width = widthBuffer.get(0);
            height = heightBuffer.get(0);
        } catch (Exception e) {
            throw new RuntimeException("Error loading icon image", e);
        }

        GLFWImage.Buffer imageBuffer = GLFWImage.malloc(1);
        GLFWImage icon = imageBuffer.get(0);
        icon.set(width, height, decodedImage);

        GLFW.glfwSetWindowIcon(windowPtr, imageBuffer);

        STBImage.stbi_image_free(decodedImage);
        imageBuffer.free();

        // Set window settings
        glfwSetWindowAspectRatio(windowPtr, 16, 9);
        glfwSetWindowSizeLimits(windowPtr, (int) (vidMode.width() * 0.5D), (int) ((vidMode.width() * 0.5D) / (16.f/9.f)), GLFW_DONT_CARE, GLFW_DONT_CARE);

        glfwSetScrollCallback(windowPtr, scrollCallback);
        glfwSetMouseButtonCallback(windowPtr, mouseButtonCallback);
        glfwSetCursorPosCallback(windowPtr, cursorPosCallback);

        glfwMakeContextCurrent(windowPtr);
        glfwSwapInterval(1);
        glfwShowWindow(windowPtr);

        GL.createCapabilities();
    }

    private void initImGui() {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        ImGuiStyle style = ImGui.getStyle();
        ImFontAtlas fontAtlas = io.getFonts();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.setConfigViewportsNoTaskBarIcon(true);
        FloatBuffer scaleX = BufferUtils.createFloatBuffer(1);
        FloatBuffer scaleY = BufferUtils.createFloatBuffer(1);
        glfwGetWindowContentScale(windowPtr, scaleX, scaleY);
        scale = Math.max(scaleX.get(), scaleY.get());
        style.scaleAllSizes(scale);
        fontAtlas.addFontDefault();
        bodyFont = fontAtlas.addFontFromMemoryTTF(Util.loadFont("WorkSans.ttf"), 16 * scale);
        titleFont = fontAtlas.addFontFromMemoryTTF(Util.loadFont("Inter.ttf"), 18 * scale);
        fontAtlas.build();

//        style.setFramePadding(4.0f, 2.0f);
        style.setItemSpacing(6.0f, 2.0f);
        style.setItemInnerSpacing(8.0f, 4.0f);
//        style.setWindowRounding(4.0f);
//        style.setFrameRounding(2.0f);
        style.setIndentSpacing(100.0f);
//        style.setColumnsMinSpacing(50.0f);
//        style.setGrabMinSize(14.0f);
//        style.setGrabRounding(16.0f);
        style.setScrollbarSize(12.0f);
        style.setScrollbarRounding(16.0f);

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

    public void close() {
        glfwSetWindowShouldClose(windowPtr, true);
    }

    public void run() {
        double time = 0;
        while(!glfwWindowShouldClose(windowPtr)) {
            Timer.calculateDeltaTime();
            fpsTimer += Timer.getDeltaTime();
            if(fpsTimer > 1) {
                fpsTimer = 0;
                glfwSetWindowTitle(windowPtr, "RayDream - " + fps + " FPS");
                fps = 0;
            }
            fps++;
            glClearColor(0.1F, 0.09F, 0.1F, 1.0F);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            imGuiGlfw.newFrame();
            ImGui.newFrame();

            MenuBar.show(this);
            PropWindow.show();
            EditorWindow.show(editorFrameBuffer);
            ObjectWindow.show();
            SettingsWindow.show();
            PreviewWindow.show(previewFrameBuffer);

            ImGui.render();
            imGuiGl3.renderDrawData(ImGui.getDrawData());

            time += Timer.getDeltaTime();
            if (time > 0.016) {
                // Render the editor window
                editorFrameBuffer.bind();
                glViewport(0, 0, (int) EditorWindow.getWidth(), (int) EditorWindow.getHeight());
                drawLights(EditorWindow.getCamera());
                drawCamera();
                drawObjects(EditorWindow.getCamera());
                editorFrameBuffer.unbind();

                // Render the preview window
                previewFrameBuffer.bind();
                glViewport(0, 0, (int) PreviewWindow.getFrameWidth() - 1, (int) PreviewWindow.getFrameHeight() - 1);
                drawLights(PreviewWindow.getCamera());
                drawObjects(PreviewWindow.getCamera());
                previewFrameBuffer.unbind();

                time = 0;
            }

            if(ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
                final long backupWindowPtr = glfwGetCurrentContext();
                ImGui.updatePlatformWindows();
                ImGui.renderPlatformWindowsDefault();
                glfwMakeContextCurrent(backupWindowPtr);
            }

            glfwSwapBuffers(windowPtr);
            glfwPollEvents();
        }
    }

    private void drawCamera() {
        objectShader.use();
        objectShader.setVec3("ambientLight.color", SettingsWindow.getAmbientColor());
        objectShader.setVec3("material.color", new float[]{1.f, 1.f, 1.f});
        objectShader.setFloat("material.ambient", 0.5f);
        objectShader.setFloat("material.diffuse", 0.5f);
        objectShader.setFloat("material.specular", 0.5f);
        objectShader.setFloat("material.specularExponent", 32);
        objectShader.setFloat("material.metalness", 0);
        objectShader.setFloat("opacity", 1.f);
        objectShader.setMatrix4("model", PreviewWindow.getCamera().getModelMatrix().getMatrixArray());
        objectShader.setMatrix4("view", EditorWindow.getCamera().getViewMatrix().getMatrixArray());
        objectShader.setMatrix4("projection", EditorWindow.getCamera().getProjectionMatrix().getMatrixArray());
        objectShader.setInt("tex", 0);
        objectShader.setBool("hasTexture", false);
        objectShader.setBool("isSelected", false);
        PreviewWindow.getCamera().draw();
        objectShader.unuse();
    }

    private void drawLights(EditorCamera camera) {
        lightShader.use();
        lightShader.setMatrix4("view", camera.getViewMatrix().getMatrixArray());
        lightShader.setMatrix4("projection", camera.getProjectionMatrix().getMatrixArray());
        for(EditorLight light : ObjectWindow.lights) {
            if(light.isSelected()) {
                glDisable(GL_DEPTH_TEST);
                lightShader.setBool("isSelected", true);
                float[] matrix = light.getModelMatrix().clone();
                matrix[0] *= 1.02F;
                matrix[5] *= 1.02F;
                matrix[10] *= 1.02F;
                lightShader.setMatrix4("model", matrix);
                light.draw();
                glEnable(GL_DEPTH_TEST);
            }
            lightShader.setBool("isSelected", false);
            lightShader.setMatrix4("model", light.getModelMatrix());
            lightShader.setVec3("color", light.getMaterial().getColor());
            light.draw();
        }
        lightShader.unuse();
    }

    private void drawObjects(EditorCamera camera) {
        objectShader.use();
        int n = 0;
        for(EditorLight light : ObjectWindow.lights) {
            objectShader.setVec3("lights[" + n + "].position", new float[]{
                    (float) light.getTransform().translation().x,
                    (float) light.getTransform().translation().y,
                    (float) light.getTransform().translation().z
            });
            objectShader.setVec3("lights[" + n + "].color", light.getMaterial().getColor());
            objectShader.setFloat("lights[" + n + "].brightness", light.getMaterial().getBrightness());
            n++;
        }
        if(ObjectWindow.lights.isEmpty()) {
            for(int i = 0; i < 100; i++) {
                objectShader.setFloat("lights[" + i + "].brightness", 0.f);
            }
        }
        objectShader.setVec3("ambientLight.color", SettingsWindow.getAmbientColor());
        Map<Float, EditorObject> transparentObjects = new HashMap<>();
        // Draw opaque objects
        for(EditorObject object : ObjectWindow.objects) {
            if(object instanceof ModelEditorObject) {
                drawObject(object, camera);
                continue;
            }
            if(object.getMaterial().getType().equals(Material.Type.REFLECT_REFRACT)) {
                Matrix4F modelMatrix = new Matrix4F(object.getModelMatrix()).transpose();
                Vector4F temp = new Vector4F(0, 0, 0, 1).mult(modelMatrix).mult(camera.getViewMatrix().transpose());
                Vector3F point = new Vector3F(temp.x / temp.w, temp.y / temp.w, temp.z / temp.w);
                float distance = point.length();
                transparentObjects.put(distance, object);
                continue;
            }
            objectShader.setFloat("opacity", 1.f);
            drawObject(object, camera);
        }

        // Draw transparent objects
        transparentObjects = new TreeMap<>(transparentObjects);
        for(int i = transparentObjects.size() - 1; i >= 0; i--) {
            EditorObject object = transparentObjects.values().stream().toList().get(i);
            objectShader.setFloat("opacity", 0.75f);
            drawObject(object, camera);
        }
        objectShader.unuse();
    }

    private void drawObject(EditorObject object, EditorCamera camera) {
        objectShader.setMatrix4("view", camera.getViewMatrix().getMatrixArray());
        objectShader.setMatrix4("projection", camera.getProjectionMatrix().getMatrixArray());
        if(object.isSelected()) {
            glDisable(GL_DEPTH_TEST);
            objectShader.setBool("isSelected", true);
            float[] matrix = object.getModelMatrix().clone();
            matrix[0] *= 1.02F;
            matrix[5] *= 1.02F;
            matrix[10] *= 1.02F;
            objectShader.setMatrix4("model", matrix);
            object.draw();
            glEnable(GL_DEPTH_TEST);
        }
        objectShader.setBool("isSelected", false);
        objectShader.setMatrix4("model", object.getModelMatrix());
        if(object instanceof ModelEditorObject) {
            for(MeshModel.Mesh mesh : ((MeshModel) object.getModel()).getMeshes()) {
                updateObjectShader(mesh.getMaterial());
                if(mesh.getMaterial().getType().equals(Material.Type.REFLECT_REFRACT)) {
                    objectShader.setFloat("opacity", 0.75f);
                } else {
                    objectShader.setFloat("opacity", 1.f);
                }
                mesh.draw();
            }
            return;
        }
        updateObjectShader(object.getMaterial());
        object.draw();
    }

    private void updateObjectShader(EditorObjectMaterial material) {
        objectShader.setVec3("material.color", material.getColor());
        objectShader.setFloat("material.ambient", material.getAmbient());
        objectShader.setFloat("material.diffuse", material.getDiffuse());
        objectShader.setFloat("material.specular", material.getSpecular());
        objectShader.setFloat("material.specularExponent", material.getSpecularExponent());
        objectShader.setFloat("material.metalness", material.getMetalness());
        objectShader.setInt("tex", 0);
        objectShader.setBool("hasTexture", material.getTexture() != null);
    }

    public float getScale() {
        return scale;
    }

    public static ImFont getTitleFont() {
        return titleFont;
    }

    public static ImFont getBodyFont() {
        return bodyFont;
    }
}
