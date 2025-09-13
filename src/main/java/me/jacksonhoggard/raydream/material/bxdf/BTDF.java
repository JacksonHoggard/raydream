package me.jacksonhoggard.raydream.material.bxdf;

import java.util.HashMap;

import me.jacksonhoggard.raydream.math.Vector3D;

public abstract class BTDF extends BxDF {

    public BTDF(Vector3D ng, Vector3D ns,
                Vector3D baseColor,
                HashMap<String, Object> parameters
        ) {
        super(
            ng, ns,
            baseColor,
            parameters
        );
    }

    protected static Vector3D refract(Vector3D v, Vector3D m, double ior) {
        Vector3D vNeg = v.negated();
        double cosI = Math.clamp(vNeg.dot(m), -1.0, 1.0);
        double etaI = 1.0;
        double etaT = ior;
        Vector3D n = new Vector3D(m);
        if (cosI < 0.0) {
            cosI = -cosI;
        } else {
            double temp = etaI;
            etaI = etaT;
            etaT = temp;
            n = n.negated();
        }
        double eta = etaI / etaT;
        double k = 1.0 - eta * eta * (1.0 - cosI * cosI);
        if (k < 0.0)
            return null; // TIR
        return vNeg.mult(eta).add(n.mult(eta * cosI - Math.sqrt(k)));
    }

}
