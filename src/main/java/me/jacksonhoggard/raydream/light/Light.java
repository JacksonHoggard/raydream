package me.jacksonhoggard.raydream.light;

import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Primitive;
import me.jacksonhoggard.raydream.object.Transform;

public abstract class Light extends Primitive implements ILight {
    private final Vector3D position;
    private final Vector3D color;
    private final double brightness;
    protected double area;

    public Light(Transform transform, Vector3D min, Vector3D max, Vector3D color, double brightness) {
        super(transform, min, max, true);
        this.color = color;
        this.brightness = brightness;
        this.position = new Vector3D();
        this.area = 1.0D;
    }

    public Light(Vector3D position, Vector3D color, double brightness) {
        super(new Transform(position, new Vector3D(0,0,0), new Vector3D(1,1,1)), new Vector3D(0,0,0), new Vector3D(0,0,0), true);
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
