package object;

import material.Material;
import math.Ray;
import math.Vector2D;
import math.Vector3D;
import math.Vector4D;

public class Sphere extends Object {
    private final double radius;

    public Sphere(Transform transform, double radius, Material material) {
        super(transform, material);
        this.radius = radius;
    }

    @Override
    public double intersect(Ray ray) {
        Vector3D oc = ray.getOrigin();
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
        Vector3D normal = point.normalized();
        Vector4D normalWS = new Vector4D(normal.x, normal.y, normal.z, 0);
        normalWS = normalWS.mult(getNormalMatrix());
        return new Vector3D(normalWS.x, normalWS.y, normalWS.z);
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
