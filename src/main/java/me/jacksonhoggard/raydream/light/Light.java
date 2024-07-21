package me.jacksonhoggard.raydream.light;

import me.jacksonhoggard.raydream.math.Vector3D;

public abstract class Light implements ILight {
    private final Vector3D position;
    private final Vector3D color;
    private final double brightness;

    public Light(Vector3D position, Vector3D color, double brightness) {
        this.position = position;
        this.color = color;
        this.brightness = brightness;
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
}
