package util;

import material.texture.Texture;
import object.Mesh;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Util {
    public static double randomRange(double min, double max) {
        return min + (max - min) * Math.random();
    }

    public static Texture loadTexture(String path) throws IOException {
        BufferedImage image = ImageIO.read(new File(path));
        return new Texture(image, image.getWidth(), image.getHeight());
    }

    public static Mesh loadOBJ(String path) {
        return OBJLoader.meshFromOBJ(path);
    }
}
