package me.jacksonhoggard.raydream.gui;

import imgui.ImGui;
import me.jacksonhoggard.raydream.SceneManager;
import me.jacksonhoggard.raydream.gui.editor.window.DialogWindow;

public class MenuBar {

    private static float height;

    public static void show(Window window) {
        if(ImGui.beginMainMenuBar()) {
            height = ImGui.getFrameHeight();
            if(ImGui.beginMenu("File")) {
                if(ImGui.menuItem("New")) {
                    newScene();
                }
                if(ImGui.menuItem("Save")) {
                    saveScene();
                }
                if(ImGui.menuItem("Open")) {
                    loadScene();
                }
                ImGui.endMenu();
            }
            if(ImGui.beginMenu("Options")) {
                if(ImGui.menuItem("Quit")) {
                    window.close();
                }
                ImGui.endMenu();
            }
            ImGui.endMainMenuBar();
        }
    }

    private static void newScene() {
        boolean confirm = DialogWindow.openConfirmation("Are you sure you want to create a new project?");
        if(confirm)
            SceneManager.newScene();
    }

    private static void saveScene() {
        String path = DialogWindow.openFileSave("New_Project.dream", "dream");
        if(path != null)
            SceneManager.saveScene(path);
    }

    private static void loadScene() {
        String path = DialogWindow.openFileChooser("Project Files", "dream");
        if(path != null)
            SceneManager.loadScene(path);
    }

    public static float getHeight() {
        return height;
    }
}