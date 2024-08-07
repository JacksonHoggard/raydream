package me.jacksonhoggard.raydream.light;

import me.jacksonhoggard.raydream.math.Matrix4D;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.math.Vector4D;
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
        double rotX = Math.toRadians(transform.rotation().x);
        double rotY = Math.toRadians(transform.rotation().y);
        double rotZ = Math.toRadians(transform.rotation().z);
        this.transform = transform;
        this.transformMatrix = (new Matrix4D(
                transform.scale().x, 0, 0, 0,
                0, transform.scale().y, 0, 0,
                0, 0, transform.scale().z, 0,
                0, 0, 0, 1
        ).mult(
                new Matrix4D(
                        Math.cos(rotY)*Math.cos(rotZ), Math.sin(rotX)*Math.sin(rotY)*Math.cos(rotZ) - Math.cos(rotX)*Math.sin(rotZ), Math.cos(rotX)*Math.sin(rotY)*Math.cos(rotZ) + Math.sin(rotX)*Math.sin(rotZ), 0,
                        Math.cos(rotY)*Math.sin(rotZ), Math.sin(rotX)*Math.sin(rotY)*Math.sin(rotZ) + Math.cos(rotX)*Math.cos(rotZ), Math.cos(rotX)*Math.sin(rotY)*Math.sin(rotZ) - Math.sin(rotX)*Math.cos(rotZ), 0,
                        -Math.sin(rotY), Math.sin(rotX)*Math.cos(rotY), Math.cos(rotX)*Math.cos(rotY), 0,
                        0, 0, 0, 1
                )
        ).mult(
                new Matrix4D(
                        1, 0, 0, transform.translation().x,
                        0, 1, 0, transform.translation().y,
                        0, 0, 1, transform.translation().z,
                        0, 0, 0, 1
                )
        ));
        this.inverseTransformMatrix = transformMatrix.inverse();
        this.getPosition().set(Object.transformPointToOS(new Vector3D(), transformMatrix));
        this.t0 = new Triangle(
                new Vector3D(-0.5, 0.5, 0),
                new Vector3D(-0.5, -0.5, 0),
                new Vector3D(0.5, -0.5, 0)
        );
        this.t1 = new Triangle(
                new Vector3D(0.5, -0.5, 0),
                new Vector3D(0.5, 0.5, 0),
                new Vector3D(-0.5, 0.5, 0)
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
