package me.jacksonhoggard.raydream.light;

import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.util.Util;

public class SphereLight extends Light {

    private final double radius;

    public SphereLight(Vector3D position, Vector3D color, double brightness, double radius) {
        super(position, color, brightness);
        this.radius = radius;
    }

    @Override
    public double intersect(Ray ray) {
        Vector3D oc = Vector3D.sub(ray.origin(), getPosition());
        double a = ray.direction().dot(ray.direction());
        double b = 2.0D * oc.dot(ray.direction());
        double c = oc.dot(oc) - radius * radius;
        double discriminant = b * b - 4 * a * c;
        if(discriminant < 0)
            return -1.0D;
        return (-b - Math.sqrt(discriminant)) / (2.0D * a);
    }

    @Override
    public Vector3D pointOnLight(int i, int j, int cols, int rows) {
        Vector3D random = Util.randomUnitVector();
        return Vector3D.add(getPosition(), random.mult(radius - 0.00001D));
    }

    public double getRadius() {
        return radius;
    }
}
