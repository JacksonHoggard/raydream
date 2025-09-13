package me.jacksonhoggard.raydream.material.bxdf.disney;

import java.util.HashMap;

import me.jacksonhoggard.raydream.material.bxdf.BRDF;
import me.jacksonhoggard.raydream.math.Vector3D;

public class DisneyClearcoat extends BRDF {
    public DisneyClearcoat(
        Vector3D ng, Vector3D ns,
        Vector3D baseColor,
        HashMap<String, Object> parameters
    ) {
        super(ng, ns, baseColor, parameters);
    }

    @Override
    public Vector3D eval(Vector3D wo, Vector3D wi) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'eval'");
    }

    @Override
    public double pdf(Vector3D wo, Vector3D wi) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pdf'");
    }

    @Override
    public BxDFSample sample(Vector3D wo) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sample'");
    }
}
