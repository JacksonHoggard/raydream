package me.jacksonhoggard.raydream.gui;

import imgui.ImGui;
import me.jacksonhoggard.raydream.SceneManager;
import me.jacksonhoggard.raydream.gui.editor.window.DialogWindow;

import java.io.IOException;

public class MenuBar {

    private static float height;

    public static void show(Window window) {
        ImGui.pushFont(Window.getTitleFont());
        if(ImGui.beginMainMenuBar()) {
            height = ImGui.getFrameHeight();
            if(ImGui.beginMenu("File")) {
                ImGui.pushFont(Window.getBodyFont());
                if(ImGui.menuItem("New")) {
                    newScene();
                }
                if(ImGui.menuItem("Save")) {
                    try {
                        saveScene();
                    } catch (IOException e) {
                        DialogWindow.showError("Unable to save project.", e);
                    }
                }
                if(ImGui.menuItem("Open")) {
                    try {
                        loadScene();
                    } catch (IOException e) {
                        DialogWindow.showError("Unable to load project.", e);
                    }
                }
                ImGui.popFont();
                ImGui.endMenu();
            }
            if(ImGui.beginMenu("Options")) {
                ImGui.pushFont(Window.getBodyFont());
                if(ImGui.menuItem("Quit")) {
                    window.close();
                }
                ImGui.popFont();
                ImGui.endMenu();
            }
            ImGui.endMainMenuBar();
        }
        ImGui.popFont();
    }

    private static void newScene() {
        boolean confirm = DialogWindow.openConfirmation("Are you sure you want to create a new project?");
        if(confirm)
            SceneManager.newScene();
    }

    private static void saveScene() throws IOException {
        String path = DialogWindow.openFolder("Choose project folder");
        if(path != null)
            SceneManager.saveScene(path);
    }

    private static void loadScene() throws IOException {
        String path = DialogWindow.openFileChooser("Project Files", "dream");
        if(path != null)
            SceneManager.loadScene(path);
    }

    public static float getHeight() {
        return height;
    }
}