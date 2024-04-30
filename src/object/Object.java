package object;

import material.Material;
import math.Matrix4D;

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
