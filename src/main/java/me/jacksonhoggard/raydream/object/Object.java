package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Matrix4D;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.math.Vector4D;

public abstract class Object implements IObject {

    private final Transform transform;
    private final Material material;
    private final Matrix4D inverseTransformMatrix;
    private final Matrix4D normalMatrix;
    private final Vector3D centroid;
    private final Vector3D min;
    private final Vector3D max;

    public Object(Transform transform, Material material, Vector3D min, Vector3D max) {
        this.transform = transform;
        this.material = material;
        double rotX = Math.toRadians(transform.rotation().x);
        double rotY = Math.toRadians(transform.rotation().y);
        double rotZ = Math.toRadians(transform.rotation().z);
        Matrix4D transformMatrix = (new Matrix4D(
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
        this.normalMatrix = inverseTransformMatrix.transpose();
        this.min = new Vector3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        this.max = new Vector3D(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
        Vector3D[] aabb = {
                min,
                new Vector3D(min.x, max.y, min.z),
                new Vector3D(max.x, min.y, min.z),
                new Vector3D(max.x, max.y, min.z),
                new Vector3D(min.x, min.y, max.z),
                new Vector3D(min.x, max.y, max.z),
                new Vector3D(max.x, min.y, max.z),
                max
        };
        for(Vector3D v : aabb) {
            v = transformPointToOS(v, transformMatrix);
            this.min.x = Math.min(this.min.x, v.x);
            this.min.y = Math.min(this.min.y, v.y);
            this.min.z = Math.min(this.min.z, v.z);
            this.max.x = Math.max(this.max.x, v.x);
            this.max.y = Math.max(this.max.y, v.y);
            this.max.z = Math.max(this.max.z, v.z);
        }
        this.centroid = Vector3D.add(this.min, this.max).div(2.0D);
    }

    public static Vector3D transformNormalToWS(Vector3D normal, Matrix4D normalMatrix) {
        Vector4D normalWS = new Vector4D(normal.x, normal.y, normal.z, 0);
        normalWS = normalWS.mult(normalMatrix);
        return new Vector3D(normalWS.x, normalWS.y, normalWS.z).normalize();
    }

    public static Vector3D transformPointToOS(Vector3D point, Matrix4D inverseTransformMatrix) {
        Vector4D pointWS = new Vector4D(point.x, point.y, point.z, 1);
        Vector4D pointOS = pointWS.mult(inverseTransformMatrix);
        return new Vector3D(pointOS.x, pointOS.y, pointOS.z);
    }
    public Material getMaterial() {
        return material;
    }

    public Transform getTransform() {
        return transform;
    }

    public Matrix4D getInverseTransformMatrix() {
        return inverseTransformMatrix;
    }

    public Matrix4D getNormalMatrix() {
        return normalMatrix;
    }

    public Vector3D getCentroid() {
        return centroid;
    }

    public Vector3D getMin() {
        return min;
    }

    public Vector3D getMax() {
        return max;
    }
}
