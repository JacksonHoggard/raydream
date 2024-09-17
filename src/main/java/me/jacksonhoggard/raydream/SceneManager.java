package me.jacksonhoggard.raydream;

import me.jacksonhoggard.raydream.gui.editor.EditorCamera;
import me.jacksonhoggard.raydream.gui.editor.light.EditorLight;
import me.jacksonhoggard.raydream.gui.editor.object.EditorObject;
import me.jacksonhoggard.raydream.light.Light;
import me.jacksonhoggard.raydream.light.PointLight;
import me.jacksonhoggard.raydream.material.Reflective;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Box;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.object.Transform;
import me.jacksonhoggard.raydream.render.Camera;
import me.jacksonhoggard.raydream.render.Scene;

import java.io.IOException;
import java.util.List;

public class SceneManager {

    public static void newScene() {

    }

    public static void saveScene(String path) {

    }

    public static void loadScene(String path) {

    }

    public static void renderScene(List<EditorObject> editorObjects, List<EditorLight> editorLights, Light ambient, Vector3D skyColor, EditorCamera editorCamera, int width, int height, float aperture, String filename, int sampleDepth, int bounces, int numShadowRays, int threads) {
        Object[] objects = new Object[editorObjects.size()];
        for(int i = 0; i < objects.length; i++) {
            objects[i] = editorObjects.get(i).toObject();
        }
        Light[] lights = new Light[editorLights.size()];
        for(int i = 0; i < lights.length; i++) {
            lights[i] = editorLights.get(i).toLight();
        }
        Camera camera = new Camera(editorCamera.getLookFrom(), editorCamera.getLookAt(), editorCamera.getUp(), editorCamera.getFov(), aperture, width, height);
        Scene scene = new Scene(camera, ambient, lights, objects, skyColor, width, height);
        try {
            scene.render(filename, sampleDepth, bounces, numShadowRays, threads);
        } catch (IOException e) {
            throw new RuntimeException("Unable to render scene: ", e);
        }
    }
}
