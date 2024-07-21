package me.jacksonhoggard.raydream.util;

import me.jacksonhoggard.raydream.material.texture.Texture;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Mesh;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Util {
    public static double randomRange(double min, double max) {
        return min + (max - min) * Math.random();
    }

    public static Vector3D randomUnitVector() {
        double theta = randomRange(0, 2.0D * Math.PI);
        double r = Math.sqrt(randomRange(0, 1.0D));
        double z = Math.sqrt(1.0D - r*r);
        if(randomRange(0, 1.0D) < 0.5D)
            z *= -1.0D;
        return new Vector3D(r * Math.cos(theta), r * Math.sin(theta), z);
    }

    public static Texture loadTexture(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        return new Texture(image, image.getWidth(), image.getHeight());
    }

    public static Mesh loadOBJ(String path) {
        return OBJLoader.meshFromOBJ(path);
    }
}
