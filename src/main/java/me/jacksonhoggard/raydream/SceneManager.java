package me.jacksonhoggard.raydream;

import me.jacksonhoggard.raydream.gui.editor.EditorCamera;
import me.jacksonhoggard.raydream.gui.editor.light.EditorLight;
import me.jacksonhoggard.raydream.gui.editor.object.EditorObject;
import me.jacksonhoggard.raydream.gui.editor.object.ModelEditorObject;
import me.jacksonhoggard.raydream.gui.editor.window.EditorWindow;
import me.jacksonhoggard.raydream.gui.editor.window.ObjectWindow;
import me.jacksonhoggard.raydream.gui.editor.window.SettingsWindow;
import me.jacksonhoggard.raydream.light.Light;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Model;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.render.Camera;
import me.jacksonhoggard.raydream.util.ProgressListener;
import me.jacksonhoggard.raydream.render.Scene;
import me.jacksonhoggard.raydream.util.io.SceneReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SceneManager {

    private static String projectDir;

    public static void newScene() {
        ObjectWindow.reset();
        EditorObject.reset();
        EditorLight.reset();
        SettingsWindow.reset();
        EditorWindow.reset();
        projectDir = System.getProperty("user.dir");
    }

    public static void saveScene(String path) throws IOException {
        Paths.get(path).toFile().mkdir();
        FileWriter writer = new FileWriter(path + File.separator + "project.dream");
        projectDir = path;

        writer.write(SettingsWindow.toSaveEntry());
        for(EditorObject object : ObjectWindow.objects) {
            writer.write("\n");
            writer.write(object.toSaveEntry(path));
        }
        for(EditorLight light : ObjectWindow.lights) {
            writer.write("\n");
            writer.write(light.toSaveEntry());
        }

        writer.close();
    }

    public static void loadScene(String path) throws IOException {
        newScene();
        projectDir = Paths.get(path).getParent().toString();
        SceneReader.read(path);
    }

    public static void renderScene(List<EditorObject> editorObjects, List<EditorLight> editorLights, Light ambient, Vector3D skyColor, EditorCamera editorCamera, int width, int height, float aperture, String filename, int sampleDepth, int bounces, int numShadowRays, int threads, ProgressListener progressListener) {
        List<Object> listObj = new ArrayList<>();
        for (EditorObject editorObject : editorObjects) {
            if (editorObject instanceof ModelEditorObject obj) {
                Model[] models = obj.toObjects();
                listObj.addAll(Arrays.asList(models));
                continue;
            }
            listObj.add(editorObject.toObject());
        }
        Object[] objects = new Object[listObj.size()];
        for(int i = 0; i < objects.length; i++) {
            objects[i] = listObj.get(i);
        }
        Light[] lights = new Light[editorLights.size()];
        for(int i = 0; i < lights.length; i++) {
            lights[i] = editorLights.get(i).toLight();
        }
        Camera camera = new Camera(editorCamera.getLookFrom(), editorCamera.getLookAt(), editorCamera.getUp(), editorCamera.getFov(), aperture, width, height);
        Scene scene = new Scene(camera, ambient, lights, objects, skyColor, width, height);
        try {
            scene.render(filename, sampleDepth, bounces, numShadowRays, threads, progressListener);
        } catch (IOException e) {
            throw new RuntimeException("Unable to render scene: ", e);
        }
    }

    public static String getProjectDir() {
        return projectDir;
    }
}
