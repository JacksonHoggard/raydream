package me.jacksonhoggard.raydream.gui;

import imgui.*;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import me.jacksonhoggard.raydream.gui.editor.EditorCamera;
import me.jacksonhoggard.raydream.gui.editor.light.EditorAreaLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorPointLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorSphereLight;
import me.jacksonhoggard.raydream.gui.editor.object.*;
import me.jacksonhoggard.raydream.gui.editor.window.*;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.*;
import me.jacksonhoggard.raydream.render.FrameBuffer;
import me.jacksonhoggard.raydream.render.Shader;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.nio.FloatBuffer;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private final ImGuiImplGlfw imGuiGlfw;
    private final ImGuiImplGl3 imGuiGl3;
    private Shader objectShader;
    private Shader lightShader;
    private FrameBuffer editorFrameBuffer;
    private FrameBuffer previewFrameBuffer;
    private static float scale;

    private String glslVersion = null;
    private static long windowPtr;

    public Window() {
        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();
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
        GLFWErrorCallback.createPrint(System.err).set();

        if(!glfwInit()) {
            System.out.println("Unable to initialize GLFW");
            System.exit(-1);
        }

        glslVersion = "#version 330";
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        windowPtr = glfwCreateWindow((int) (vidMode.width() * 0.8D), (int) (vidMode.height() * 0.8D), "RayDream", NULL, NULL);

        if(windowPtr == NULL) {
            System.out.println("Unable to create window");
            System.exit(-1);
        }

        glfwSetWindowAspectRatio(windowPtr, 16, 9);
        glfwSetWindowSizeLimits(windowPtr, (int) (vidMode.width() * 0.5D), (int) ((vidMode.width() * 0.5D) / (16.f/9.f)), GLFW_DONT_CARE, GLFW_DONT_CARE);

        glfwMakeContextCurrent(windowPtr);
        glfwSwapInterval(1);
        glfwShowWindow(windowPtr);

        GL.createCapabilities();
    }

    private void initImGui() {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        ImGuiStyle style = ImGui.getStyle();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.setConfigViewportsNoTaskBarIcon(true);
        FloatBuffer scaleX = BufferUtils.createFloatBuffer(1);
        FloatBuffer scaleY = BufferUtils.createFloatBuffer(1);
        glfwGetWindowContentScale(windowPtr, scaleX, scaleY);
        scale = Math.max(scaleX.get(), scaleY.get());
        style.scaleAllSizes(scale);
        io.setFontGlobalScale(scale);
    }

    public void close() {
        glfwSetWindowShouldClose(windowPtr, true);
    }

    public static void pause() {
        glfwHideWindow(windowPtr);
    }

    public static void resume() {
        glfwShowWindow(windowPtr);
    }

    public void run() {
        while(!glfwWindowShouldClose(windowPtr)) {
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
        PreviewWindow.getCamera().draw();
        objectShader.unuse();
    }

    private void drawLights(EditorCamera camera) {
        lightShader.use();
        lightShader.setMatrix4("view", camera.getViewMatrix().getMatrixArray());
        lightShader.setMatrix4("projection", camera.getProjectionMatrix().getMatrixArray());
        for(EditorLight light : ObjectWindow.lights) {
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
        objectShader.setVec3("material.color", object.getMaterial().getColor());
        objectShader.setFloat("material.ambient", object.getMaterial().getAmbient());
        objectShader.setFloat("material.diffuse", object.getMaterial().getDiffuse());
        objectShader.setFloat("material.specular", object.getMaterial().getSpecular());
        objectShader.setFloat("material.specularExponent", object.getMaterial().getSpecularExponent());
        objectShader.setFloat("material.metalness", object.getMaterial().getMetalness());
        objectShader.setMatrix4("model", object.getModelMatrix());
        objectShader.setMatrix4("view", camera.getViewMatrix().getMatrixArray());
        objectShader.setMatrix4("projection", camera.getProjectionMatrix().getMatrixArray());
        object.draw();
    }

    public static float getScale() {
        return scale;
    }
}
