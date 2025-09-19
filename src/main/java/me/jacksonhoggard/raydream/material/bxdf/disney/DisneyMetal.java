package me.jacksonhoggard.raydream.material.bxdf.disney;

import java.util.HashMap;

import me.jacksonhoggard.raydream.material.bxdf.BRDF;
import me.jacksonhoggard.raydream.math.Vector3D;

public class DisneyMetal extends BRDF {

    private final double roughness; // [0..1]
    private final double anisotropic; // [0..1]

    public DisneyMetal(
        Vector3D ng, Vector3D ns,
        Vector3D baseColor,
        HashMap<String, Object> parameters
    ) {
        super(ng, ns, baseColor, parameters);
        this.roughness = ((Double) parameters.get("roughness"));
        this.anisotropic = ((Double) parameters.get("anisotropic"));
    }

    @Override
    public Vector3D eval(Vector3D wo, Vector3D wi) {
        Vector3D h = Vector3D.add(wo, wi).normalized();
        Vector3D Fm = Vector3D.add(baseColor, new Vector3D(1).sub(baseColor).mult(pow5(1 - Math.abs(h.dot(wi)))));
        
        Vector3D hLocal = toLocal(h, ns).normalize();
        Vector3D woLocal = toLocal(wo, ns).normalize();
        Vector3D wiLocal = toLocal(wi, ns).normalize();

        double aspect = Math.sqrt(1.0D - 0.9D * anisotropic);
        double ax = Math.max(0.0001D, sqr(roughness) / aspect);
        double ay = Math.max(0.0001D, sqr(roughness) * aspect);

        double D = 1.0D / (Math.PI * ax * ay * Math.pow(
            (hLocal.x * hLocal.x) / (ax * ax) +
            (hLocal.y * hLocal.y) / (ay * ay) +
            (hLocal.z * hLocal.z), 2));

        double lambdaL = (Math.sqrt(1.0 + (sqr(wiLocal.x*ax) + sqr(wiLocal.y*ay))/sqr(wiLocal.z)) - 1.0D) / 2.0D;
        double lambdaV = (Math.sqrt(1.0 + (sqr(woLocal.x*ax) + sqr(woLocal.y*ay))/sqr(woLocal.z)) - 1.0D) / 2.0D;
        double GL = 1.0D / (1.0D + lambdaL);
        double GV = 1.0D / (1.0D + lambdaV);
        double G = GL * GV;

        Vector3D fMetal = Vector3D.mult(Fm, D * G).div(4.0D * Math.abs(ns.dot(wo)));

        return fMetal;
    }

    @Override
    public double pdf(Vector3D wo, Vector3D wi) {
        Vector3D h = Vector3D.add(wo, wi).normalized();
        
        Vector3D hLocal = toLocal(h, ns).normalize();
        Vector3D woLocal = toLocal(wo, ns).normalize();

        double aspect = Math.sqrt(1.0D - 0.9D * anisotropic);
        double ax = Math.max(0.0001D, sqr(roughness) / aspect);
        double ay = Math.max(0.0001D, sqr(roughness) * aspect);

        double D = 1.0D / (Math.PI * ax * ay * Math.pow(
            (hLocal.x * hLocal.x) / (ax * ax) +
            (hLocal.y * hLocal.y) / (ay * ay) +
            (hLocal.z * hLocal.z), 2));

        double lambdaV = (Math.sqrt(1.0 + (sqr(woLocal.x*ax) + sqr(woLocal.y*ay))/sqr(woLocal.z)) - 1.0D) / 2.0D;
        double GV = 1.0D / (1.0D + lambdaV);

        return (D * GV) / (4.0D * Math.abs(ns.dot(wo)));
    }

    @Override
    public BxDFSample sample(Vector3D wo) {
        Vector3D randomNormal = toWorld(ns, sampleVndfGGX(wo, Math.max(sqr(roughness), 0.0001D))).normalize();
        Vector3D wi = reflect(wo, randomNormal);
        Vector3D f = eval(wo, wi);
        double pdf = pdf(wo, wi);
        return new BxDFSample(wi, f, pdf, Event.REFLECT, sqr(roughness) <= 0.0001D);
    }
}
