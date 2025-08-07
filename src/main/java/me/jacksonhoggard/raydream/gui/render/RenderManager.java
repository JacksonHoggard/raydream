package me.jacksonhoggard.raydream.gui.render;

import me.jacksonhoggard.raydream.gui.config.LayoutConfig;
import me.jacksonhoggard.raydream.gui.editor.EditorCamera;
import me.jacksonhoggard.raydream.gui.editor.light.EditorLight;
import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.MeshModel;
import me.jacksonhoggard.raydream.gui.editor.object.EditorObject;
import me.jacksonhoggard.raydream.gui.editor.object.ModelEditorObject;
import me.jacksonhoggard.raydream.gui.editor.window.ObjectWindow;
import me.jacksonhoggard.raydream.gui.editor.window.PreviewWindow;
import me.jacksonhoggard.raydream.gui.editor.window.SettingsWindow;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Matrix4F;
import me.jacksonhoggard.raydream.math.Vector3F;
import me.jacksonhoggard.raydream.math.Vector4F;
import me.jacksonhoggard.raydream.render.FrameBuffer;
import me.jacksonhoggard.raydream.render.Shader;
import me.jacksonhoggard.raydream.util.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static org.lwjgl.opengl.GL11.*;

public class RenderManager implements AutoCloseable {
    private final Logger logger;
    private Shader objectShader;
    private Shader lightShader;
    private FrameBuffer editorFrameBuffer;
    private FrameBuffer previewFrameBuffer;
    private double lastRenderTime = 0;
    
    public RenderManager(Logger logger) {
        this.logger = logger;
    }
    
    public void initialize() {
        try {
            objectShader = new Shader("object_vertex_shader.glsl", "object_fragment_shader.glsl");
            lightShader = new Shader("light_vertex_shader.glsl", "light_fragment_shader.glsl");
            editorFrameBuffer = new FrameBuffer(1920, 1080);
            previewFrameBuffer = new FrameBuffer(1920, 1080);
            logger.info("Render manager initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize render manager", e);
            throw new RuntimeException("Render manager initialization failed", e);
        }
    }
    
    public void render(double currentTime, EditorCamera editorCamera, EditorCamera previewCamera, 
                      float editorWidth, float editorHeight, float previewWidth, float previewHeight) {
        
        // Only render at the specified interval
        if (currentTime - lastRenderTime < LayoutConfig.RENDER_INTERVAL) {
            return;
        }
        
        try {
            renderEditorView(editorCamera, editorWidth, editorHeight);
            renderPreviewView(previewCamera, previewWidth, previewHeight);
            lastRenderTime = currentTime;
        } catch (Exception e) {
            logger.error("Render error", e);
        }
    }
    
    private void renderEditorView(EditorCamera camera, float width, float height) {
        editorFrameBuffer.bind();
        glViewport(0, 0, (int) width, (int) height);
        
        drawLights(camera);
        drawCamera(camera);
        drawObjects(camera);
        
        editorFrameBuffer.unbind();
    }
    
    private void renderPreviewView(EditorCamera camera, float width, float height) {
        previewFrameBuffer.bind();
        glViewport(0, 0, (int) width - 1, (int) height - 1);
        
        drawLights(camera);
        drawObjects(camera);
        
        previewFrameBuffer.unbind();
    }
    
    private void drawCamera(EditorCamera editorCamera) {
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
        objectShader.setMatrix4("view", editorCamera.getViewMatrix().getMatrixArray());
        objectShader.setMatrix4("projection", editorCamera.getProjectionMatrix().getMatrixArray());
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
        
        for (EditorLight light : ObjectWindow.lights) {
            if (light.isSelected()) {
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
        
        // Setup lights for objects
        setupLightsForObjects();
        objectShader.setVec3("ambientLight.color", SettingsWindow.getAmbientColor());
        
        // Separate opaque and transparent objects
        Map<Float, EditorObject> transparentObjects = new HashMap<>();
        
        // Draw opaque objects
        for (EditorObject object : ObjectWindow.objects) {
            if (object instanceof ModelEditorObject) {
                drawObject(object, camera);
                continue;
            }
            
            if (object.getMaterial().getType().equals(Material.Type.REFLECT_REFRACT)) {
                float distance = calculateObjectDistance(object, camera);
                transparentObjects.put(distance, object);
                continue;
            }
            
            objectShader.setFloat("opacity", 1.f);
            drawObject(object, camera);
        }
        
        // Draw transparent objects (back to front)
        drawTransparentObjects(transparentObjects, camera);
        
        objectShader.unuse();
    }
    
    private void setupLightsForObjects() {
        int n = 0;
        for (EditorLight light : ObjectWindow.lights) {
            objectShader.setVec3("lights[" + n + "].position", new float[]{
                    (float) light.getTransform().translation().x,
                    (float) light.getTransform().translation().y,
                    (float) light.getTransform().translation().z
            });
            objectShader.setVec3("lights[" + n + "].color", light.getMaterial().getColor());
            objectShader.setFloat("lights[" + n + "].brightness", light.getMaterial().getBrightness());
            n++;
        }
        
        if (ObjectWindow.lights.isEmpty()) {
            for (int i = 0; i < 100; i++) {
                objectShader.setFloat("lights[" + i + "].brightness", 0.f);
            }
        }
    }
    
    private float calculateObjectDistance(EditorObject object, EditorCamera camera) {
        Matrix4F modelMatrix = new Matrix4F(object.getModelMatrix()).transpose();
        Vector4F temp = new Vector4F(0, 0, 0, 1).mult(modelMatrix).mult(camera.getViewMatrix().transpose());
        Vector3F point = new Vector3F(temp.x / temp.w, temp.y / temp.w, temp.z / temp.w);
        return point.length();
    }
    
    private void drawTransparentObjects(Map<Float, EditorObject> transparentObjects, EditorCamera camera) {
        transparentObjects = new TreeMap<>(transparentObjects);
        for (int i = transparentObjects.size() - 1; i >= 0; i--) {
            EditorObject object = transparentObjects.values().stream().toList().get(i);
            objectShader.setFloat("opacity", 0.75f);
            drawObject(object, camera);
        }
    }
    
    private void drawObject(EditorObject object, EditorCamera camera) {
        objectShader.setMatrix4("view", camera.getViewMatrix().getMatrixArray());
        objectShader.setMatrix4("projection", camera.getProjectionMatrix().getMatrixArray());
        
        if (object.isSelected()) {
            drawSelectedOutline(object);
        }
        
        objectShader.setBool("isSelected", false);
        objectShader.setMatrix4("model", object.getModelMatrix());
        
        if (object instanceof ModelEditorObject) {
            drawModelObject((ModelEditorObject) object);
        } else {
            updateObjectShader(object.getMaterial());
            object.draw();
        }
    }
    
    private void drawSelectedOutline(EditorObject object) {
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
    
    private void drawModelObject(ModelEditorObject modelObject) {
        for (MeshModel.Mesh mesh : ((MeshModel) modelObject.getModel()).getMeshes()) {
            updateObjectShader(mesh.getMaterial());
            if (mesh.getMaterial().getType().equals(Material.Type.REFLECT_REFRACT)) {
                objectShader.setFloat("opacity", 0.75f);
            } else {
                objectShader.setFloat("opacity", 1.f);
            }
            mesh.draw();
        }
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
    
    public FrameBuffer getEditorFrameBuffer() {
        return editorFrameBuffer;
    }
    
    public FrameBuffer getPreviewFrameBuffer() {
        return previewFrameBuffer;
    }
    
    @Override
    public void close() {
        logger.info("Starting render manager cleanup...");
        
        try {
            if (objectShader != null) {
                objectShader.cleanup();
                objectShader = null;
                logger.debug("Object shader cleaned up");
            }
        } catch (Exception e) {
            logger.error("Error cleaning up object shader", e);
        }
        
        try {
            if (lightShader != null) {
                lightShader.cleanup();
                lightShader = null;
                logger.debug("Light shader cleaned up");
            }
        } catch (Exception e) {
            logger.error("Error cleaning up light shader", e);
        }
        
        try {
            if (editorFrameBuffer != null) {
                editorFrameBuffer.cleanup();
                editorFrameBuffer = null;
                logger.debug("Editor framebuffer cleaned up");
            }
        } catch (Exception e) {
            logger.error("Error cleaning up editor framebuffer", e);
        }
        
        try {
            if (previewFrameBuffer != null) {
                previewFrameBuffer.cleanup();
                previewFrameBuffer = null;
                logger.debug("Preview framebuffer cleaned up");
            }
        } catch (Exception e) {
            logger.error("Error cleaning up preview framebuffer", e);
        }
        
        logger.info("Render manager cleanup complete");
    }
}
