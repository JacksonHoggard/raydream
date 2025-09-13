package me.jacksonhoggard.raydream.material.bxdf;

import java.util.ArrayList;
import java.util.HashMap;

import me.jacksonhoggard.raydream.math.Vector3D;

public abstract class BSDF extends BxDF {

    protected final ArrayList<BxDF> bxdfs = new ArrayList<>();

    public BSDF(
        Vector3D ng, Vector3D ns,
        Vector3D baseColor,
        HashMap<String, Object> parameters
    ) {
        super(
            ng, ns, baseColor, parameters
        );
    }

}
