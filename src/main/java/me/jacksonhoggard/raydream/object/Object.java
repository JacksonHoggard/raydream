package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Vector3D;

public abstract class Object extends Primitive implements IObject {

    private final Material material;

    public Object(Transform transform, Material material, Vector3D min, Vector3D max) {
        super(transform, min, max, false);
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }
}
