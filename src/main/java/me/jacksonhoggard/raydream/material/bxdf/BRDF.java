package me.jacksonhoggard.raydream.material.bxdf;

import java.util.HashMap;

import me.jacksonhoggard.raydream.math.Vector3D;

public abstract class BRDF extends BxDF {
    
    public BRDF(
            Vector3D ng, Vector3D ns,
            Vector3D baseColor,
            HashMap<String, Object> parameters) {
        super(
                ng, ns,
                baseColor,
                parameters
        );
    }

    protected static Vector3D reflect(Vector3D v, Vector3D m) {
        Vector3D vNeg = v.negated();
        return vNeg.sub(Vector3D.mult(m, 2*vNeg.dot(m))).normalized();
    }
    
}
