package me.jacksonhoggard.raydream;

import me.jacksonhoggard.raydream.core.ApplicationContext;
import me.jacksonhoggard.raydream.service.SceneService;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @deprecated Use SceneService through ApplicationContext instead.
 * This class is kept for backward compatibility but delegates to the new service.
 */
@Deprecated
public class SceneManager {

    private static final SceneService sceneService = ApplicationContext.getInstance().getSceneService();

    public static void newScene() {
        sceneService.createNewScene();
    }

    public static void saveScene(String path) throws IOException {
        sceneService.saveScene(Paths.get(path));
    }

    public static void loadScene(String path) throws IOException {
        sceneService.loadScene(Paths.get(path));
    }

    public static String getProjectDir() {
        return sceneService.getCurrentProjectDirectory().toString();
    }
}
