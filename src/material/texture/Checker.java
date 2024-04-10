package material.texture;

import math.Vector3D;

public class Checker extends Texture {

    private final Vector3D colorA;
    private final Vector3D colorB;
    private final int width;
    private final int height;

    public Checker(int width, int height, Vector3D colorA, Vector3D colorB) {
        this.colorA = colorA;
        this.colorB = colorB;
        this.width = width;
        this.height = height;
    }

    @Override
    protected Vector3D uvPatternAt(double u, double v) {
        double u2 = Math.floor(u * width);
        double v2 = Math.floor(v * height);

        if((u2 + v2) % 2 == 0)
            return colorA;
        return colorB;
    }

}
