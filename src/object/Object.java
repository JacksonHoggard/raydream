package object;

import material.Material;
import math.Matrix4D;
import math.Vector3D;
import math.Vector4D;

public abstract class Object implements IObject {

    private final Transform transform;
    private final Material material;
    private final Matrix4D inverseTransformMatrix;
    private final Matrix4D normalMatrix;

    public Object(Transform transform, Material material) {
        this.transform = transform;
        this.material = material;
        double rotX = Math.toRadians(transform.rotation().x);
        double rotY = Math.toRadians(transform.rotation().y);
        double rotZ = Math.toRadians(transform.rotation().z);
        this.inverseTransformMatrix = (new Matrix4D(
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
        )).inverse();
        this.normalMatrix = inverseTransformMatrix.transpose();
    }

    public static Vector3D transformNormalToWS(Vector3D normal, Matrix4D normalMatrix) {
        Vector4D normalWS = new Vector4D(normal.x, normal.y, normal.z, 0);
        normalWS = normalWS.mult(normalMatrix);
        return new Vector3D(normalWS.x, normalWS.y, normalWS.z).normalize();
    }

    public static Vector3D transformPointToOS(Vector3D point, Matrix4D inverseTransformMatrix) {
        Vector4D normalWS = new Vector4D(point.x, point.y, point.z, 1);
        Vector4D pointOS = normalWS.mult(inverseTransformMatrix);
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
}
