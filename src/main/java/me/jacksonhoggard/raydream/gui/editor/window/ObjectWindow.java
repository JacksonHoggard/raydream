package me.jacksonhoggard.raydream.gui.editor.window;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import me.jacksonhoggard.raydream.gui.MenuBar;
import me.jacksonhoggard.raydream.gui.editor.object.BoxEditorObject;
import me.jacksonhoggard.raydream.gui.editor.object.EditorObject;
import me.jacksonhoggard.raydream.gui.editor.object.PlaneEditorObject;
import me.jacksonhoggard.raydream.gui.editor.object.SphereEditorObject;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class ObjectWindow {

    public static final ArrayList<EditorObject> objects = new ArrayList<>();

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
            if(ImGui.beginMenu("Add")) {
                if(ImGui.menuItem("Box")) {
                    objects.add(new BoxEditorObject());
                    EditorObject.setSelected(objects.getLast().getId());
                }
                if(ImGui.menuItem("Sphere")) {
                    objects.add(new SphereEditorObject());
                    EditorObject.setSelected(objects.getLast().getId());
                }
                if(ImGui.menuItem("Plane")) {
                    objects.add(new PlaneEditorObject());
                    EditorObject.setSelected(objects.getLast().getId());
                }
                ImGui.endMenu();
            }
            if(ImGui.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
                EditorObject.setSelected(-1);
            }
            if(ImGui.isKeyPressed(GLFW.GLFW_KEY_DELETE)) {
                objects.remove(getSelectedObject());
            }
            ImGui.separator();
            for(EditorObject object : objects) {
                object.show();
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
}
