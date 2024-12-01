package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public class Plane extends Object {

    private final Vector3D normal;

    public Plane(double offset, Vector3D rotation, Material material) {
        super(
                new Transform(
                        new Vector3D(0, offset, 0),
                        rotation,
                        new Vector3D(1, 1, 1)),
                material,
                new Vector3D(-10000, -1, -10000),
                new Vector3D(10000, 0, 10000)
        );
        this.normal = new Vector3D(0, 1, 0);
    }

    @Override
    public Hit intersect(Ray ray) {
        double t = (-ray.origin().dot(normal)) / ray.direction().dot(normal);
        return new Hit(
                this, null, ray.at(t), normal, mapTexture(ray.at(t)), t
        );
    }

    @Override
    public Vector2D mapTexture(Vector3D point) {
        if(point.x < 0) point.x = 1+(point.x % 1.0D);
        if(point.z < 0) point.z = 1+(point.z % 1.0D);
        return new Vector2D(point.x % 1.0D, point.z % 1.0D);
    }

    @Override
    public Vector3D calcTangent(Vector3D normal) {
        return new Vector3D(-1, 0, 0);
    }

    @Override
    public Vector3D calcBitangent(Vector3D normal, Vector3D tangent) {
        return new Vector3D(0, 0, -1);
    }
}
