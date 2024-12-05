package me.jacksonhoggard.raydream.material;

import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

import java.awt.image.BufferedImage;

public class BumpMap extends Texture {

    private final double scale;
    private final double deltaX, deltaY;

    public BumpMap(BufferedImage image, String path, int width, int height, double scale) {
        super(image, path, width, height);
        this.scale = scale;
        this.deltaX = 1.d / width;
        this.deltaY = 1.d / height;
    }

    public Vector3D apply(Vector3D normal, Vector3D tangent, Vector3D bitangent, Vector2D texCoords) {
        double u = adjustUV(texCoords.x);
        double v = adjustUV(texCoords.y);
        double u1 = adjustUV(texCoords.x + deltaX);
        double v1 = adjustUV(texCoords.y + deltaY);
        int x = (int) Math.floor(u * (getWidth() - 1));
        int y = (int) Math.floor(v * (getHeight() - 1));
        int x1 = (int) Math.floor(u1 * (getWidth() - 1));
        int y1 = (int) Math.floor(v1 * (getHeight() - 1));
        double height = getHeightAt(x, y);
        double dU = getHeightAt(x1, y) - height;
        double dV = getHeightAt(x, y1) - height;
        dU *= scale;
        dV *= scale;
        Vector3D qu = Vector3D.add(tangent, Vector3D.mult(normal, -dU));
        Vector3D qv = Vector3D.add(bitangent, Vector3D.mult(normal, -dV));
        return qv.cross(qu).normalize();
    }

    private double getHeightAt(int x, int y) {
        int color = getImage().getRGB(x, y);
        return ((((color & 0xff0000) >> 16) / 255D)
                + (((color & 0xff00) >> 8) / 255D)
                + (((color & 0xff)) / 255D)) / 3.0D;
    }
}
