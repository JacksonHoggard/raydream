package me.jacksonhoggard.raydream.gui.editor.window;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import me.jacksonhoggard.raydream.gui.MenuBar;
import me.jacksonhoggard.raydream.gui.Window;
import me.jacksonhoggard.raydream.gui.editor.light.EditorAreaLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorPointLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorSphereLight;
import me.jacksonhoggard.raydream.gui.editor.model.OBJModel;
import me.jacksonhoggard.raydream.gui.editor.model.RDOModel;
import me.jacksonhoggard.raydream.gui.editor.object.*;
import org.lwjgl.glfw.GLFW;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class ObjectWindow {

    public static final ArrayList<EditorObject> objects = new ArrayList<>();
    public static final ArrayList<EditorLight> lights = new ArrayList<>();

    private static float width;
    private static float height;
    private static float posX;
    private static float posY;

    public static void show() {
        width = ImGui.getMainViewport().getSizeX() - (EditorWindow.getWidth() + PropWindow.getWidth());
        height = (ImGui.getMainViewport().getSizeY() - MenuBar.getHeight()) / 2;
        posX = EditorWindow.getPosX() + EditorWindow.getWidth();
        posY = MenuBar.getHeight();
        ImGui.setNextWindowSize(width, height);
        ImGui.setNextWindowPos(posX, posY);
        if(ImGui.begin("Objects", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoBringToFrontOnFocus)) {
            ImGui.pushFont(Window.getTitleFont());
            if(ImGui.beginMenu("Add Object")) {
                ImGui.pushFont(Window.getBodyFont());
                if(ImGui.menuItem("Box")) {
                    try {
                        objects.add(new BoxEditorObject());
                        EditorObject.setSelected(objects.getLast().getId());
                        EditorLight.setSelected(-1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(ImGui.menuItem("Sphere")) {
                    try {
                        objects.add(new SphereEditorObject());
                        EditorObject.setSelected(objects.getLast().getId());
                        EditorLight.setSelected(-1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(ImGui.menuItem("Plane")) {
                    try {
                        objects.add(new PlaneEditorObject());
                        EditorObject.setSelected(objects.getLast().getId());
                        EditorLight.setSelected(-1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(ImGui.menuItem("Model")) {
                    String path = DialogWindow.openFileChooser("3D models", "obj", "rdo");
                    if(path != null) {
                        try {
                            System.out.println("Attempting to load model: " + path);
                            if(path.endsWith(".obj")) {
                                ModelEditorObject modelObj = new ModelEditorObject(new OBJModel(path, new FileInputStream(path)));
                                objects.add(modelObj);
                                System.out.println("Successfully loaded OBJ model: " + path);
                            } else if(path.endsWith(".rdo")) {
                                ModelEditorObject modelObj = new ModelEditorObject(new RDOModel(path, new FileInputStream(path)));
                                objects.add(modelObj);
                                System.out.println("Successfully loaded RDO model: " + path);
                            } else {
                                DialogWindow.showError("Unsupported file format", new Exception("Only .obj and .rdo files are supported"));
                                return; // Exit the menu item handler
                            }
                            EditorObject.setSelected(objects.getLast().getId());
                            EditorLight.setSelected(-1);
                        } catch (FileNotFoundException e) {
                            DialogWindow.showError("File not found: " + path, e);
                            System.err.println("File not found: " + path);
                        } catch (IOException e) {
                            DialogWindow.showError("Unable to load model: " + e.getMessage(), e);
                            System.err.println("IOException loading model: " + e.getMessage());
                            e.printStackTrace();
                        } catch (Exception e) {
                            DialogWindow.showError("Unexpected error loading model: " + e.getMessage(), e);
                            System.err.println("Unexpected error loading model: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
                ImGui.popFont();
                ImGui.endMenu();
            }
            if(ImGui.beginMenu("Add Light")) {
                ImGui.pushFont(Window.getBodyFont());
                if(ImGui.menuItem("Point")) {
                    try {
                        lights.add(new EditorPointLight());
                        EditorLight.setSelected(lights.getLast().getId());
                        EditorObject.setSelected(-1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(ImGui.menuItem("Sphere")) {
                    try {
                        lights.add(new EditorSphereLight());
                        EditorLight.setSelected(lights.getLast().getId());
                        EditorObject.setSelected(-1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                if(ImGui.menuItem("Area")) {
                    try {
                        lights.add(new EditorAreaLight());
                        EditorLight.setSelected(lights.getLast().getId());
                        EditorObject.setSelected(-1);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                ImGui.popFont();
                ImGui.endMenu();
            }
            if(ImGui.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                EditorObject.setSelected(-1);
                EditorLight.setSelected(-1);
            }
            if(ImGui.isKeyPressed(GLFW.GLFW_KEY_DELETE)) {
                if(getSelectedObject() instanceof ModelEditorObject)
                    getSelectedObject().remove();
                objects.remove(getSelectedObject());
                lights.remove(getSelectedLight());
            }
            ImGui.separator();
            ImGui.pushFont(Window.getBodyFont());
            for(EditorObject object : objects) {
                object.show();
                ImGui.separator();
            }
            for(EditorLight light : lights) {
                light.show();
                ImGui.separator();
            }
            ImGui.popFont();

            ImGui.popFont();
        }
        ImGui.end();
    }

    public static void reset() {
        objects.clear();
        lights.clear();
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

    public static EditorObject getSelectedObject() {
        for(EditorObject object : objects) {
            if(object.getId() == EditorObject.getSelected())
                return object;
            if(!object.getSubIds().isEmpty()) {
                for(Integer i : object.getSubIds()) {
                    if(i.intValue() == EditorObject.getSelected())
                        return object;
                }
            }
        }
        return null;
    }

    public static EditorLight getSelectedLight() {
        for(EditorLight light : lights) {
            if(light.getId() == EditorLight.getSelected())
                return light;
        }
        return null;
    }
}
