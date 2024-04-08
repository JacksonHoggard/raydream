package object;

import material.Material;
import math.Ray;
import math.Vector3D;

public class Sphere extends Object {
    private final double radius;

    public Sphere(Vector3D position, double radius, Material material) {
        super(position, material);
        this.radius = radius;
    }

    @Override
    public double intersect(Ray ray) {
        Vector3D oc = Vector3D.sub(ray.getOrigin(), getPosition());
        double a = ray.getDirection().dot(ray.getDirection());
        double b = 2.0D * oc.dot(ray.getDirection());
        double c = oc.dot(oc) - radius * radius;
        double discriminant = b * b - 4 * a * c;
        if(discriminant < 0)
            return -1.0D;
        return (-b - Math.sqrt(discriminant)) / (2.0D * a);
    }

    @Override
    public Vector3D normalAt(Vector3D point) {
        return Vector3D.sub(point, getPosition()).div(radius);
    }
}
