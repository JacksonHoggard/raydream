package me.jacksonhoggard.raydream;

import me.jacksonhoggard.raydream.light.Light;
import me.jacksonhoggard.raydream.light.PointLight;
import me.jacksonhoggard.raydream.material.Reflective;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Box;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.object.Transform;
import me.jacksonhoggard.raydream.render.Camera;
import me.jacksonhoggard.raydream.render.Scene;

public class SceneManager {

    private static Scene currentScene;

    public static void newScene() {
        currentScene = new Scene(
                new Camera(new Vector3D(0, 0, -2), new Vector3D(0, 0, 0), new Vector3D(0, 1, 0), 60, 100, 800, 600),
                new PointLight(new Vector3D(0, 2, 0), new Vector3D(1, 1, 1), 1),
                new Light[]{},
                new Object[]{
                        new Box(
                                new Transform(new Vector3D(0, 0, 0), new Vector3D(0, 0, 0), new Vector3D(1, 1, 1)),
                                new Vector3D(1, 1, 1),
                                new Reflective(new Vector3D(1, 0, 0), 0.1, 0.4, 0.5, 32, 0.3, 0.177, 3.638)
                        )
                },
                800,
                600
        );
    }

    public static void saveScene(String path) {
        currentScene.save(path);
    }

    public static void loadScene(String path) {
        currentScene = Scene.read(path);
    }

    public static Scene getCurrentScene() {
        return currentScene;
    }
}
