package me.jacksonhoggard.raydream.gui;

import imgui.ImFont;
import imgui.ImGui;
import me.jacksonhoggard.raydream.core.ApplicationContext;
import me.jacksonhoggard.raydream.gui.editor.window.DialogWindow;
import me.jacksonhoggard.raydream.service.SceneService;

import java.io.IOException;
import java.nio.file.Paths;

public class MenuBar {

    private static float height;
    private static final SceneService sceneService = ApplicationContext.getInstance().getSceneService();

    public static void show(Window window) {
        ImFont titleFont = Window.getTitleFont();
        ImFont bodyFont = Window.getBodyFont();
        
        ImGui.pushFont(titleFont != null ? titleFont : ImGui.getFont());
        if(ImGui.beginMainMenuBar()) {
            height = ImGui.getFrameHeight();
            if(ImGui.beginMenu("File")) {
                ImGui.pushFont(bodyFont != null ? bodyFont : ImGui.getFont());
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
            ImGui.endMainMenuBar();
        }
        ImGui.popFont();
    }

    private static void newScene() {
        boolean confirm = DialogWindow.openConfirmation("Are you sure you want to create a new project?");
        if(confirm)
            sceneService.createNewScene();
    }

    private static void saveScene() throws IOException {
        String path = DialogWindow.openFolder("Choose project folder");
        if(path != null)
            sceneService.saveScene(Paths.get(path));
    }

    private static void loadScene() throws IOException {
        String path = DialogWindow.openFileChooser("Project Files", "dream");
        if(path != null)
            sceneService.loadScene(Paths.get(path));
    }

    public static float getHeight() {
        return height;
    }
}