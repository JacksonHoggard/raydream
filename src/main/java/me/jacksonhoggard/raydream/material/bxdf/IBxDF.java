package me.jacksonhoggard.raydream.material.bxdf;

import me.jacksonhoggard.raydream.material.bxdf.BxDF.BxDFSample;
import me.jacksonhoggard.raydream.math.Vector3D;

public interface IBxDF {
    Vector3D eval(Vector3D wo, Vector3D wi);
    double pdf(Vector3D wo, Vector3D wi);
    BxDFSample sample(Vector3D wo);
}
