package me.jacksonhoggard.raydream.util;

import me.jacksonhoggard.raydream.material.BumpMap;
import me.jacksonhoggard.raydream.material.Texture;
import me.jacksonhoggard.raydream.math.Vector3D;

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

    public static BumpMap loadBumpMap(String path, double bumpScale) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load bump map file:" + path, e);
        }
        return new BumpMap(image, path, image.getWidth(), image.getHeight(), bumpScale);
    }

    public static String loadShader(InputStream shaderInput) {
        try {
            return new String(shaderInput.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load shader file.", e);
        }
    }

    public static List<String> readAllLines(InputStream inputStream) {
        List<String> list = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while((line = br.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Failed to read resource file.", e);
        }
        return list;
    }
}
