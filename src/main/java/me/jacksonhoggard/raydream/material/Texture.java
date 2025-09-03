package me.jacksonhoggard.raydream.material;

import me.jacksonhoggard.raydream.math.Vector2D;
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

    public Vector3D sample(Vector2D uv) {
        double u = adjustUV(uv.x);
        double v = adjustUV(uv.y);
        
        // Calculate texture coordinates
        double x = u * (width - 1);
        double y = v * (height - 1);
        
        // Get integer and fractional parts
        int x0 = (int) Math.floor(x);
        int y0 = (int) Math.floor(y);
        int x1 = Math.min(x0 + 1, width - 1);
        int y1 = Math.min(y0 + 1, height - 1);
        
        double fx = x - x0;
        double fy = y - y0;
        
        // Sample four neighboring pixels
        Vector3D c00 = getPixelColor(x0, y0);
        Vector3D c10 = getPixelColor(x1, y0);
        Vector3D c01 = getPixelColor(x0, y1);
        Vector3D c11 = getPixelColor(x1, y1);
        
        // Bilinear interpolation
        Vector3D top = Vector3D.mult(c00, 1 - fx).add(Vector3D.mult(c10, fx));
        Vector3D bottom = Vector3D.mult(c01, 1 - fx).add(Vector3D.mult(c11, fx));
        
        return top.mult(1 - fy).add(bottom.mult(fy));
    }
    
    private Vector3D getPixelColor(int x, int y) {
        int color = image.getRGB(x, y);
        return new Vector3D(
                ((color & 0xff0000) >> 16) / 255D,
                ((color & 0xff00) >> 8) / 255D,
                ((color & 0xff)) / 255D
        );
    }

    protected Vector3D getColorAt(double u, double v) {
        u = adjustUV(u);
        v = adjustUV(v);
        int x = (int) Math.floor(u * (width - 1));
        int y = (int) Math.floor(v * (height - 1));
        int color = image.getRGB(x, y);
        return new Vector3D(
                ((color & 0xff0000) >> 16) / 255D,
                ((color & 0xff00) >> 8) / 255D,
                ((color & 0xff)) / 255D
        );
    }

    protected static double adjustUV(double u) {
        while(u > 1.0d)
            u -= 1.0d;
        while(u < 0.0d)
            u += 1.0d;
        return u;
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
