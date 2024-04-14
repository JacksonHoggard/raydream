package material.texture;

import math.Vector2D;
import math.Vector3D;
import object.Object;

import java.awt.image.BufferedImage;

public class Texture {

    private final BufferedImage image;
    private final int width;
    private final int height;

    public Texture(BufferedImage image, int width, int height) {
        this.image = image;
        this.width = width;
        this.height = height;
    }

    public Vector3D textureAt(Object object, Vector3D point) {
        Vector2D map = object.mapTexture(point);
        return getColorAt(map.x, map.y);
    }

    protected Vector3D getColorAt(double u, double v) {
        int x = (int) Math.round(u * (width - 1));
        int y = (int) Math.round((1 - v) * (height - 1));
        int color = image.getRGB(x, y);
        return new Vector3D(
                ((color & 0xff0000) >> 16) / 255D,
                ((color & 0xff00) >> 8) / 255D,
                ((color & 0xff)) / 255D
        );
    }

}
