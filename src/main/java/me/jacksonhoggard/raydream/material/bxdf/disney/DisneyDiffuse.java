package me.jacksonhoggard.raydream.material.bxdf.disney;

import java.util.HashMap;

import me.jacksonhoggard.raydream.material.bxdf.BRDF;
import me.jacksonhoggard.raydream.math.Vector3D;

public class DisneyDiffuse extends BRDF {

    private final double roughness; // [0..1]
    private final double subsurface; // [0..1]

    public DisneyDiffuse(
            Vector3D ng, Vector3D ns,
            Vector3D baseColor, HashMap<String, Object> parameters
    ) {
        super(ng, ns, baseColor, parameters);
        this.roughness = ((Double) parameters.get("roughness"));
        this.subsurface = ((Double) parameters.get("subsurface"));
    }

    @Override
    public Vector3D eval(Vector3D wo, Vector3D wi) {
        Vector3D baseDiffColor = Vector3D.mult(baseColor, 1.0 / Math.PI);

        Vector3D h = Vector3D.add(wo, wi).normalized();

        double fd90 = 0.5 + 2.0 * roughness * sqr(Math.abs(h.dot(wi)));
        double fdV = 1.0 + (fd90 - 1.0D) * pow5(1.0 - Math.abs(wo.dot(ns)));
        double fdL = 1.0 + (fd90 - 1.0D) * pow5(1.0 - Math.abs(wo.dot(ns)));
        double fd = fdV * fdL;

        Vector3D baseDiffuse = Vector3D.mult(baseDiffColor, fd * (Math.abs(ns.dot(wi))));

        Vector3D baseSubColor = Vector3D.mult(baseColor, 1.25D).div(Math.PI);
        double fss90 = roughness * sqr(Math.abs(h.dot(wi)));
        double fssV = 1.0 + (fss90 - 1.0D) * pow5(1.0 - Math.abs(wo.dot(ns)));
        double fssL = 1.0 + (fss90 - 1.0D) * pow5(1.0 - Math.abs(wi.dot(ns)));
        double fss = fssV * fssL;
        double t1 = (1.0D / (Math.abs(wo.dot(ns)) + Math.abs(wi.dot(ns)))) - 0.5D;
        double t2 = (fss * t1) + 0.5D;
        Vector3D baseSubsurface = Vector3D.mult(baseSubColor, t2 * (Math.abs(ns.dot(wi))));

        return Vector3D.add(Vector3D.mult(1.0D - subsurface, baseDiffuse), Vector3D.mult(subsurface, baseSubsurface));
    }

    @Override
    public double pdf(Vector3D wo, Vector3D wi) {
        return Math.abs(ns.dot(wi)) / Math.PI;
    }

    @Override
    public BxDFSample sample(Vector3D wo) {
        Vector3D wi = sampleCosineHemisphere();
        if(wi.dot(ns) < 0.0D) wi.negate();
        double pdf = pdf(wo, wi);
        Vector3D f = eval(wo, wi);
        return new BxDFSample(wi, f, pdf, Event.REFLECT, false);
    }
}
