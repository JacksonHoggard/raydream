package me.jacksonhoggard.raydream;

import me.jacksonhoggard.raydream.render.Scene;
import me.jacksonhoggard.raydream.util.io.SceneReader;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        SceneReader sceneReader = new SceneReader();
        Scene scene = sceneReader.read("./example.scene");
        scene.render("output.png", 3, 8, 1, 16);
    }
}