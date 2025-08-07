package me.jacksonhoggard.raydream.service;

import me.jacksonhoggard.raydream.gui.editor.EditorCamera;
import me.jacksonhoggard.raydream.gui.editor.light.EditorLight;
import me.jacksonhoggard.raydream.gui.editor.object.EditorObject;
import me.jacksonhoggard.raydream.gui.editor.object.ModelEditorObject;
import me.jacksonhoggard.raydream.gui.editor.window.EditorWindow;
import me.jacksonhoggard.raydream.gui.editor.window.ObjectWindow;
import me.jacksonhoggard.raydream.gui.editor.window.SettingsWindow;
import me.jacksonhoggard.raydream.light.PointLight;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Model;
import me.jacksonhoggard.raydream.render.Scene;
import me.jacksonhoggard.raydream.util.ProgressListener;
import me.jacksonhoggard.raydream.util.io.SceneReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Service responsible for scene management operations.
 * Replaces the static SceneManager with a proper service-oriented approach.
 */
public class SceneService {

    private Path currentProjectDirectory;
    private boolean hasUnsavedChanges = false;

    public SceneService() {
        this.currentProjectDirectory = Paths.get(System.getProperty("user.dir"));
    }

    /**
     * Creates a new empty scene and resets all editor state.
     */
    public void createNewScene() {
        resetEditorState();
        this.currentProjectDirectory = Paths.get(System.getProperty("user.dir"));
        this.hasUnsavedChanges = false;
    }

    /**
     * Saves the current scene to the specified path.
     * @param projectPath the directory path where the project should be saved
     * @throws IOException if saving fails
     */
    public void saveScene(Path projectPath) throws IOException {
        if (!projectPath.toFile().exists()) {
            projectPath.toFile().mkdirs();
        }

        Path projectFile = projectPath.resolve("project.dream");

        try (FileWriter writer = new FileWriter(projectFile.toFile())) {
            // Write settings
            writer.write(SettingsWindow.toSaveEntry());

            // Write objects
            for (EditorObject object : ObjectWindow.objects) {
                writer.write("\n");
                writer.write(object.toSaveEntry(projectPath.toString()));
            }

            // Write lights
            for (EditorLight light : ObjectWindow.lights) {
                writer.write("\n");
                writer.write(light.toSaveEntry());
            }
        }

        this.currentProjectDirectory = projectPath;
        this.hasUnsavedChanges = false;
    }

    /**
     * Loads a scene from the specified project file.
     * @param projectFile the .dream project file to load
     * @throws IOException if loading fails
     */
    public void loadScene(Path projectFile) throws IOException {
        if (!projectFile.toFile().exists()) {
            throw new IOException("Project file does not exist: " + projectFile);
        }

        resetEditorState();

        SceneReader.read(projectFile.toString());

        this.currentProjectDirectory = projectFile.getParent();
        this.hasUnsavedChanges = false;
    }

    /**
     * Gets the current project directory.
     * @return the current project directory path
     */
    public Path getCurrentProjectDirectory() {
        return currentProjectDirectory;
    }

    /**
     * Checks if there are unsaved changes.
     * @return true if there are unsaved changes
     */
    public boolean hasUnsavedChanges() {
        return hasUnsavedChanges;
    }

    /**
     * Marks the scene as having unsaved changes.
     */
    public void markAsModified() {
        this.hasUnsavedChanges = true;
    }

    /**
     * Gets the current project name from the directory.
     * @return the project name or "Untitled" if no project is loaded
     */
    public String getCurrentProjectName() {
        if (currentProjectDirectory != null) {
            return currentProjectDirectory.getFileName().toString();
        }
        return "Untitled";
    }

    private void resetEditorState() {
        ObjectWindow.reset();
        EditorObject.reset();
        EditorLight.reset();
        SettingsWindow.reset();
        EditorWindow.reset();
    }

    public void renderScene(ArrayList<EditorObject> objects, ArrayList<EditorLight> lights, PointLight ambientLight, Vector3D skyColor, EditorCamera camera, int width, int height, float aperture, String filename, int sampleDepth, int bounces, int numShadowRays, int threads, ProgressListener progressListener) throws IOException {
        // Convert editor camera to render camera
        me.jacksonhoggard.raydream.render.Camera renderCamera = new me.jacksonhoggard.raydream.render.Camera(
            camera.getLookFrom(), 
            camera.getLookAt(), 
            new Vector3D(0, 1, 0), // up vector
            camera.getFov(),
            aperture,
            width,
            height
        );
        
        // Convert editor lights to render lights
        ArrayList<me.jacksonhoggard.raydream.light.Light> renderLightsList = new ArrayList<>();
        for (EditorLight editorLight : lights) {
            renderLightsList.add(editorLight.toLight());
        }
        me.jacksonhoggard.raydream.light.Light[] renderLights = renderLightsList.toArray(new me.jacksonhoggard.raydream.light.Light[0]);
        
        // Convert editor objects to render objects
        ArrayList<me.jacksonhoggard.raydream.object.Object> renderObjectsList = new ArrayList<>();
        for (EditorObject editorObject : objects) {
            if (editorObject instanceof ModelEditorObject) {
                // ModelEditorObject returns an array of Model objects
                ModelEditorObject modelObject = (ModelEditorObject) editorObject;
                Model[] models = modelObject.toObjects();
                for (Model model : models) {
                    renderObjectsList.add(model);
                }
            } else {
                // Other objects return a single Object
                renderObjectsList.add(editorObject.toObject());
            }
        }
        me.jacksonhoggard.raydream.object.Object[] renderObjects = renderObjectsList.toArray(new me.jacksonhoggard.raydream.object.Object[0]);
        
        Scene scene = new Scene(renderCamera, ambientLight, renderLights, renderObjects, skyColor, width, height);
        scene.render(filename, sampleDepth, bounces, numShadowRays, threads, progressListener);
    }
}
