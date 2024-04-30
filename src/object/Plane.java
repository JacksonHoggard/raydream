package object;

import material.Material;
import math.Ray;
import math.Vector2D;
import math.Vector3D;

public class Plane extends Object {

    private final double offset;
    private final Vector3D normal;

    public Plane(double offset, Vector3D normal, Material material) {
        super(new Transform(new Vector3D(0, 0, 0), new Vector3D(0, 0, 0), new Vector3D(1, 1, 1)), material);
        this.offset = offset;
        this.normal = normal.normalize();
    }

    @Override
    public double intersect(Ray ray) {
        return (offset - ray.getOrigin().dot(normal)) / ray.getDirection().dot(normal);
    }

    @Override
    public Vector3D normalAt(Vector3D point) {
        return normal;
    }

    @Override
    public Vector2D mapTexture(Vector3D point) {
        return new Vector2D(point.x % 1, point.z % 1);
    }
}
