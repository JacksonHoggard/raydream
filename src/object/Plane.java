package object;

import material.Material;
import math.Ray;
import math.Vector2D;
import math.Vector3D;

public class Plane extends Object {

    private final Vector3D normal;

    public Plane(double offset, Vector3D normal, Material material) {
        super(new Transform(new Vector3D(0, offset, 0), new Vector3D(0, 0, 0), new Vector3D(1, 1, 1)), material);
        this.normal = normal.normalize();
    }

    @Override
    public Hit intersect(Ray ray) {
        double t = (-ray.getOrigin().dot(normal)) / ray.getDirection().dot(normal);
        return new Hit(
                this, ray.at(t), normal, t
        );
    }

    @Override
    public Vector2D mapTexture(Vector3D point) {
        return new Vector2D(point.x % 1, point.z % 1);
    }
}
