package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.material.bxdf.BxDF;
import me.jacksonhoggard.raydream.math.Vector3D;

public abstract class Object extends Primitive implements IObject {

    private final Material<? extends BxDF> material;

    public Object(Transform transform, Material<? extends BxDF> material, Vector3D min, Vector3D max) {
        super(transform, min, max, false);
        this.material = material;
    }

    public Material<? extends BxDF> getMaterial() {
        return material;
    }
}
