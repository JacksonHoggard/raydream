package me.jacksonhoggard.raydream.gui.editor.window;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import me.jacksonhoggard.raydream.gui.MenuBar;
import me.jacksonhoggard.raydream.gui.editor.light.EditorAreaLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorPointLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorSphereLight;
import me.jacksonhoggard.raydream.gui.editor.object.*;
import org.lwjgl.glfw.GLFW;

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
            if(ImGui.beginMenu("Add Object")) {
                if(ImGui.menuItem("Box")) {
                    objects.add(new BoxEditorObject());
                    EditorObject.setSelected(objects.getLast().getId());
                    EditorLight.setSelected(-1);
                }
                if(ImGui.menuItem("Sphere")) {
                    objects.add(new SphereEditorObject());
                    EditorObject.setSelected(objects.getLast().getId());
                    EditorLight.setSelected(-1);
                }
                if(ImGui.menuItem("Plane")) {
                    objects.add(new PlaneEditorObject());
                    EditorObject.setSelected(objects.getLast().getId());
                    EditorLight.setSelected(-1);
                }
                if(ImGui.menuItem("Model")) {
                    String path = DialogWindow.openFileChooser("WaveFront OBJ", "obj");
                    if(path != null) {
                        objects.add(new OBJEditorObject(path));
                        EditorObject.setSelected(objects.getLast().getId());
                        EditorLight.setSelected(-1);
                    }
                }
                ImGui.endMenu();
            }
            if(ImGui.beginMenu("Add Light")) {
                if(ImGui.menuItem("Point")) {
                    lights.add(new EditorPointLight());
                    EditorLight.setSelected(lights.getLast().getId());
                    EditorObject.setSelected(-1);
                }
                if(ImGui.menuItem("Sphere")) {
                    lights.add(new EditorSphereLight());
                    EditorLight.setSelected(lights.getLast().getId());
                    EditorObject.setSelected(-1);
                }
                if(ImGui.menuItem("Area")) {
                    lights.add(new EditorAreaLight());
                    EditorLight.setSelected(lights.getLast().getId());
                    EditorObject.setSelected(-1);
                }
                ImGui.endMenu();
            }
            if(ImGui.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                EditorObject.setSelected(-1);
                EditorLight.setSelected(-1);
            }
            if(ImGui.isKeyPressed(GLFW.GLFW_KEY_DELETE)) {
                objects.remove(getSelectedObject());
                lights.remove(getSelectedLight());
            }
            ImGui.separator();
            for(EditorObject object : objects) {
                object.show();
                ImGui.separator();
            }
            for(EditorLight light : lights) {
                light.show();
                ImGui.separator();
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

    public static EditorObject getSelectedObject() {
        for(EditorObject object : objects) {
            if(object.getId() == EditorObject.getSelected())
                return object;
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
