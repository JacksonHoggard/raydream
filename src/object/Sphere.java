package object;

import material.Material;
import math.Ray;
import math.Vector2D;
import math.Vector3D;
import math.Vector4D;

public class Sphere extends Object {
    private final double radius;

    public Sphere(Transform transform, double radius, Material material) {
        super(transform, material, new Vector3D(-radius, -radius, -radius), new Vector3D(radius, radius, radius));
        this.radius = radius;
    }

    @Override
    public Hit intersect(Ray ray) {
        Vector3D oc = ray.getOrigin();
        double a = ray.getDirection().dot(ray.getDirection());
        double b = 2.0D * oc.dot(ray.getDirection());
        double c = oc.dot(oc) - radius * radius;
        double discriminant = b * b - 4 * a * c;
        if(discriminant < 0)
            return new Hit(null, null, null, -1.0D);
        double t = (-b - Math.sqrt(discriminant)) / (2.0D * a);
        return new Hit(this, ray.at(t), transformNormalToWS(ray.at(t).normalized(), getNormalMatrix()), t);
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
}
