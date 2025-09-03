package me.jacksonhoggard.raydream.light;

import me.jacksonhoggard.raydream.math.Vector3D;

public abstract class Light implements ILight {
    private final Vector3D position;
    private final Vector3D color;
    private final double brightness;
    protected double area;

    public Light(Vector3D position, Vector3D color, double brightness) {
        this.position = position;
        this.color = color;
        this.brightness = brightness;
        this.area = 1.0D;
    }

    public Vector3D getPosition() {
        return position;
    }

    public Vector3D getColor() {
        return color;
    }

    public double getBrightness() {
        return brightness;
    }

    public double getArea() {
        return area;
    }
}
