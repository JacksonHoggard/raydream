package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public class Sphere extends Object {
    private final double radius;

    public Sphere(Transform transform, double radius, Material material) {
        super(transform, material, new Vector3D(-radius, -radius, -radius), new Vector3D(radius, radius, radius));
        this.radius = radius;
    }

    @Override
    public Hit intersect(Ray ray) {
        Vector3D oc = ray.origin();
        double a = ray.direction().dot(ray.direction());
        double b = 2.0D * oc.dot(ray.direction());
        double c = oc.dot(oc) - radius * radius;
        double discriminant = b * b - 4 * a * c;
        if(discriminant < 0)
            return new Hit(null, null, null, null, null, -1.0D);
        double t = (-b - Math.sqrt(discriminant)) / (2.0D * a);
        if(t < 0)
            t = (-b + Math.sqrt(discriminant)) / (2.0D * a);
        return new Hit(this, null, ray.at(t), ray.at(t).normalized(), mapTexture(ray.at(t)), t);
    }

    public Vector3D normalAt(Vector3D point) {
        return point.normalized(); // Note: point is a point in object space
    }

    @Override
    public Vector2D mapTexture(Vector3D point) {
        Vector3D temp = this.normalAt(point).mult(radius);
        double theta = Math.atan2(temp.x, temp.z);
        double phi = Math.acos(temp.y / radius);
        double rawU = theta / (2 * Math.PI);
        double u = 1 - (rawU + 0.5D);
        double v = 1 - phi / Math.PI;
        return new Vector2D(u, v);
    }

    @Override
    public Vector3D calcTangent(Vector3D normal) {
        Vector3D temp = new Vector3D(normal).mult(radius);
        Vector3D tangent = new Vector3D(
                2 * Math.PI * temp.z,
                0,
                -2 * Math.PI * temp.x
        );
        return tangent.normalize();
    }

    @Override
    public Vector3D calcBitangent(Vector3D normal, Vector3D tangent) {
        Vector3D temp = new Vector3D(normal).mult(radius);
        double phi = Math.atan2(temp.z, temp.x);
        double theta = Math.acos(temp.y / radius);

        return new Vector3D(
                Math.PI * temp.y * Math.cos(phi),
                -radius * Math.PI * Math.sin(theta),
                Math.PI * temp.y * Math.sin(phi)
        ).normalize();
    }

    public double getRadius() {
        return radius;
    }
}
