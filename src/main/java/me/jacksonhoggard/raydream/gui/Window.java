package me.jacksonhoggard.raydream.gui;

import imgui.*;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import me.jacksonhoggard.raydream.gui.editor.EditorCamera;
import me.jacksonhoggard.raydream.gui.editor.object.EditorObject;
import me.jacksonhoggard.raydream.gui.editor.window.*;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.*;
import me.jacksonhoggard.raydream.render.FrameBuffer;
import me.jacksonhoggard.raydream.render.ShaderProgram;
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
    private ShaderProgram shaderProgram;
    private FrameBuffer editorFrameBuffer;
    private FrameBuffer previewFrameBuffer;

    private String glslVersion = null;
    private long windowPtr;

    public Window() {
        imGuiGlfw = new ImGuiImplGlfw();
        imGuiGl3 = new ImGuiImplGl3();
    }

    public void init() {
        initWindow();
        initImGui();
        imGuiGlfw.init(windowPtr, true);
        imGuiGl3.init(glslVersion);
        shaderProgram = new ShaderProgram();
        editorFrameBuffer = new FrameBuffer(1920, 1080);
        previewFrameBuffer = new FrameBuffer(1920, 1080);
    }

    public void destroy() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
        Callbacks.glfwFreeCallbacks(windowPtr);
        shaderProgram.cleanup();
        editorFrameBuffer.cleanup();
        previewFrameBuffer.cleanup();
        glfwDestroyWindow(windowPtr);
        glfwTerminate();
        for(EditorObject object : ObjectWindow.objects) {
            object.getModel().remove();
        }
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
        glfwSetWindowSizeLimits(windowPtr, 854, 480, GLFW_DONT_CARE, GLFW_DONT_CARE);

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
        float scale = Math.max(scaleX.get(), scaleY.get());
        style.scaleAllSizes(scale);
        io.setFontGlobalScale(scale);
    }

    public void close() {
        glfwSetWindowShouldClose(windowPtr, true);
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

            editorFrameBuffer.bind();
            shaderProgram.use();

            shaderProgram.setVec3("light.position", new float[]{3.f, 3.f, 3.f});
            shaderProgram.setVec3("light.color", new float[]{1.f, 1.f, 1.f});
            shaderProgram.setFloat("light.brightness", 5.f);
            shaderProgram.setVec3("ambientLight.color", new float[]{1.f, 1.f, 1.f});

            glViewport(0, 0, (int) EditorWindow.getWidth(), (int) EditorWindow.getHeight());
            drawObjects(EditorWindow.getCamera());

            editorFrameBuffer.unbind();

            // Render the preview window
            previewFrameBuffer.bind();

            glViewport(0, 0, (int) PreviewWindow.getFrameWidth(), (int) PreviewWindow.getFrameHeight());
            drawObjects(PreviewWindow.getCamera());

            shaderProgram.unuse();
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

    private void drawObjects(EditorCamera camera) {
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
            shaderProgram.setFloat("opacity", 1.f);
            drawObject(object, camera);
        }

        // Draw transparent objects
        transparentObjects = new TreeMap<>(transparentObjects);
        for(int i = transparentObjects.size() - 1; i >= 0; i--) {
            EditorObject object = transparentObjects.values().stream().toList().get(i);
            shaderProgram.setFloat("opacity", 0.75f);
            drawObject(object, camera);
        }
    }

    private void drawObject(EditorObject object, EditorCamera camera) {
        shaderProgram.setVec3("material.color", object.getMaterial().getColor());
        shaderProgram.setFloat("material.ambient", object.getMaterial().getAmbient());
        shaderProgram.setFloat("material.diffuse", object.getMaterial().getDiffuse());
        shaderProgram.setFloat("material.specular", object.getMaterial().getSpecular());
        shaderProgram.setFloat("material.specularExponent", object.getMaterial().getSpecularExponent());
        shaderProgram.setFloat("material.indexOfRefraction", object.getMaterial().getIndexOfRefraction());
        shaderProgram.setFloat("material.k", object.getMaterial().getK());
        shaderProgram.setFloat("material.metalness", object.getMaterial().getMetalness());

        shaderProgram.setMatrix4("model", object.getModelMatrix());
        shaderProgram.setMatrix4("view", camera.getViewMatrix().getMatrixArray());
        shaderProgram.setMatrix4("projection", camera.getProjectionMatrix().getMatrixArray());
        object.draw();
    }
}
