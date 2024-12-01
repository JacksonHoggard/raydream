package me.jacksonhoggard.raydream.material;

import me.jacksonhoggard.raydream.math.Vector3D;

import java.awt.image.BufferedImage;

public class Texture {

    private final BufferedImage image;
    private final String path;
    private final int width;
    private final int height;

    public Texture(BufferedImage image, String path, int width, int height) {
        this.image = image;
        this.path = path;
        this.width = width;
        this.height = height;
    }

    protected Vector3D getColorAt(double u, double v) {
        int x = (int) Math.round(u * (width - 1));
        int y = (int) Math.round(v * (height - 1));
        int color = image.getRGB(x, y);
        return new Vector3D(
                ((color & 0xff0000) >> 16) / 255D,
                ((color & 0xff00) >> 8) / 255D,
                ((color & 0xff)) / 255D
        );
    }

    public String getPath() {
        return path;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BufferedImage getImage() {
        return image;
    }
}
