package me.jacksonhoggard.raydream.light;

import me.jacksonhoggard.raydream.math.*;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.object.Transform;
import me.jacksonhoggard.raydream.object.Triangle;
import me.jacksonhoggard.raydream.util.Util;

public class AreaLight extends Light {
    private final Transform transform;
    private final Matrix4D transformMatrix;
    private final Matrix4D inverseTransformMatrix;
    private final Triangle t0, t1;

    public AreaLight(Transform transform, Vector3D color, double brightness) {
        super(new Vector3D(), color, brightness);
        this.transform = transform;
        this.transformMatrix = Object.composeModelMatrix(transform);
        this.inverseTransformMatrix = transformMatrix.inverse();
        this.getPosition().set(Object.transformPointToOS(new Vector3D(), transformMatrix));
        this.t0 = new Triangle(
                new Vector3D(-0.5, 0.5, 0),
                new Vector3D(-0.5, -0.5, 0),
                new Vector3D(0.5, -0.5, 0),
                null,
                null,
                null
        );
        this.t1 = new Triangle(
                new Vector3D(0.5, -0.5, 0),
                new Vector3D(0.5, 0.5, 0),
                new Vector3D(-0.5, 0.5, 0),
                null,
                null,
                null
        );
    }

    @Override
    public double intersect(Ray ray) {
        Vector4D rOriginOS = new Vector4D(ray.getOrigin().x, ray.getOrigin().y, ray.getOrigin().z, 1);
        Vector4D rDirOS = new Vector4D(ray.getDirection().x, ray.getDirection().y, ray.getDirection().z, 0);
        rOriginOS = rOriginOS.mult(inverseTransformMatrix);
        rDirOS = rDirOS.mult(inverseTransformMatrix);
        Ray rayOS = new Ray(new Vector3D(rOriginOS.x, rOriginOS.y, rOriginOS.z), new Vector3D(rDirOS.x, rDirOS.y, rDirOS.z));
        double t0Dist = t0.intersect(rayOS);
        double t1Dist = t1.intersect(rayOS);
        if(t0Dist <= 0.0D && t1Dist > 0.0D)
            return t1Dist;
        if(t1Dist <= 0.0D && t0Dist > 0.0D)
            return t0Dist;
        if(t0Dist > 0.0D && t1Dist > 0.0D)
            return Math.min(t0Dist, t1Dist);
        return -1.0D;
    }

    @Override
    public Vector3D pointOnLight(int i, int j, int cols, int rows) {
        double gridSizeX = 1.0D / cols;
        double gridSizeY = 1.0D / rows;
        double x = -0.5D + (i * gridSizeX) + Util.randomRange(0, gridSizeX);
        double y = -0.5D + (j * gridSizeY) + Util.randomRange(0, gridSizeY);
        Vector3D pointOS = new Vector3D(x, y, 0);
        return Object.transformPointToOS(pointOS, transformMatrix);
    }

    public Transform getTransform() {
        return transform;
    }
}
