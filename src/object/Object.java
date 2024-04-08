package object;

import material.Material;
import math.Vector3D;

public abstract class Object implements IObject {

    private final Vector3D position;
    private final Material material;

    public Object(Vector3D position, Material material) {
        this.position = position;
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public Vector3D getPosition() {
        return position;
    }
}
