package me.jacksonhoggard.raydream.util;

import me.jacksonhoggard.raydream.material.texture.Texture;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Mesh;
import me.jacksonhoggard.raydream.util.io.OBJLoader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Util {
    public static double randomRange(double min, double max) {
        return min + (max - min) * ThreadLocalRandom.current().nextDouble();
    }

    public static Vector3D randomUnitVector() {
        double theta = randomRange(0, 2.0D * Math.PI);
        double r = Math.sqrt(randomRange(0, 1.0D));
        double z = Math.sqrt(1.0D - r*r);
        if(randomRange(0, 1.0D) < 0.5D)
            z *= -1.0D;
        return new Vector3D(r * Math.cos(theta), r * Math.sin(theta), z);
    }

    public static Texture loadTexture(String path) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture file:" + path, e);
        }
        return new Texture(image, path, image.getWidth(), image.getHeight());
    }

    public static String loadShader(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load shader file:" + path, e);
        }
    }

    public static List<String> readAllLines(String path) {
        List<String> list = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while((line = br.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Failed to read resource file:" + path, e);
        }
        return list;
    }

    public static Mesh loadOBJ(String path) {
        return OBJLoader.meshFromOBJ(path);
    }
}
