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
        
        // Get roughness parameter or default to 0.5 if not provided
        Object roughnessObj = parameters != null ? parameters.get("roughness") : null;
        this.roughness = (roughnessObj instanceof Double) ? (Double) roughnessObj : 0.5;
        
        // Get anisotropic parameter or default to 0.0 if not provided
        Object anisotropicObj = parameters != null ? parameters.get("anisotropic") : null;
        this.anisotropic = (anisotropicObj instanceof Double) ? (Double) anisotropicObj : 0.0;
    }

    @Override
    public Vector3D eval(Vector3D wo, Vector3D wi) {
        Vector3D h = Vector3D.add(wo, wi).normalized();
        Vector3D Fm = Vector3D.add(baseColor, new Vector3D(1).sub(baseColor).mult(pow5(1 - absCosTheta(wi))));

        double aspect = Math.sqrt(1.0D - 0.9D * anisotropic);
        double ax = Math.max(0.0001D, sqr(roughness) / aspect);
        double ay = Math.max(0.0001D, sqr(roughness) * aspect);

        double D = 1.0D / (Math.PI * ax * ay * Math.pow(
            (h.x * h.x) / (ax * ax) +
            (h.y * h.y) / (ay * ay) +
            (h.z * h.z), 2));

        double lambdaL = (Math.sqrt(1.0 + (sqr(wi.x*ax) + sqr(wi.y*ay))/sqr(wi.z)) - 1.0D) / 2.0D;
        double lambdaV = (Math.sqrt(1.0 + (sqr(wo.x*ax) + sqr(wo.y*ay))/sqr(wo.z)) - 1.0D) / 2.0D;
        double GL = 1.0D / (1.0D + lambdaL);
        double GV = 1.0D / (1.0D + lambdaV);
        double G = GL * GV;

        Vector3D fMetal = Vector3D.mult(Fm, D * G).div(4.0D * absCosTheta(wo));

        return fMetal;
    }

    @Override
    public double pdf(Vector3D wo, Vector3D wi) {
        Vector3D h = Vector3D.add(wo, wi).normalized();

        double aspect = Math.sqrt(1.0D - 0.9D * anisotropic);
        double ax = Math.max(0.0001D, sqr(roughness) / aspect);
        double ay = Math.max(0.0001D, sqr(roughness) * aspect);

        double D = 1.0D / (Math.PI * ax * ay * Math.pow(
            (h.x * h.x) / (ax * ax) +
            (h.y * h.y) / (ay * ay) +
            (h.z * h.z), 2));

        double lambdaV = (Math.sqrt(1.0 + (sqr(wo.x*ax) + sqr(wo.y*ay))/sqr(wo.z)) - 1.0D) / 2.0D;
        double GV = 1.0D / (1.0D + lambdaV);

        return (D * GV) / (4.0D * absCosTheta(wo));
    }

    @Override
    public BxDFSample sample(Vector3D woWorld) {
        Vector3D wi;
        Vector3D wo = shadingFrame.toLocal(woWorld);
        Vector3D randomNormal = sampleVndfGGX(wo, Math.max(sqr(roughness), 0.0001D)).normalize();
        randomNormal = shadingFrame.toWorld(randomNormal);
        wi = reflect(woWorld, randomNormal);
        wi = shadingFrame.toLocal(wi);
        Vector3D f = eval(wo, wi);
        double pdf = pdf(wo, wi);
        return new BxDFSample(shadingFrame.toWorld(wi), f, pdf, Event.REFLECT, sqr(roughness) <= 0.0001D);
    }
}
