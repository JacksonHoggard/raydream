package me.jacksonhoggard.raydream.material.bxdf.disney;

import java.util.HashMap;

import me.jacksonhoggard.raydream.material.bxdf.BRDF;
import me.jacksonhoggard.raydream.math.Vector3D;

public class DisneyClearcoat extends BRDF {

    private final double clearcoatGloss; // [0..1]

    public DisneyClearcoat(
        Vector3D ng, Vector3D ns,
        Vector3D baseColor,
        HashMap<String, Object> parameters
    ) {
        super(ng, ns, baseColor, parameters);
        this.clearcoatGloss = ((Double) parameters.get("clearcoatGloss"));
    }

    @Override
    public Vector3D eval(Vector3D wo, Vector3D wi) {
        double ag = (1.0D - clearcoatGloss) * 0.1D + clearcoatGloss * 0.001D;
        Vector3D h = Vector3D.add(wo, wi).normalized();
        double Fc = 0.04D + (1.0D - 0.04D) * pow5(1 - absCosTheta(wi));
        double D = (sqr(ag) - 1.0D) / (Math.PI * Math.log(sqr(ag)) * (1.0D + (sqr(ag) - 1.0D) * (sqr(h.z))));
        double lambdaL = (Math.sqrt(1.0 + (sqr(wi.x*0.25D) + sqr(wi.y*0.25D))/sqr(wi.z)) - 1.0D) / 2.0D;
        double lambdaV = (Math.sqrt(1.0 + (sqr(wo.x*0.25D) + sqr(wo.y*0.25D))/sqr(wo.z)) - 1.0D) / 2.0D;
        double GL = 1.0D / (1.0D + lambdaL);
        double GV = 1.0D / (1.0D + lambdaV);
        double G = GL * GV;
        Vector3D fClearcoat = new Vector3D(1).mult(Fc * D * G).div(4.0D * absCosTheta(wo));
        return fClearcoat;
    }

    @Override
    public double pdf(Vector3D wo, Vector3D wi) {
        double ag = (1.0D - clearcoatGloss) * 0.1D + clearcoatGloss * 0.001D;
        Vector3D h = Vector3D.add(wo, wi).normalized();
        double D = (sqr(ag) - 1.0D) / (Math.PI * Math.log(sqr(ag)) * (1.0D + (sqr(ag) - 1.0D) * (sqr(h.z))));
        return (D * absCosTheta(h)) / (4.0D * absCosTheta(wi));
    }

    @Override
    public BxDFSample sample(Vector3D woWorld) {
        Vector3D wo = shadingFrame.toLocal(woWorld);
        Vector3D randomNormal = sampleGTR1((1.0D - clearcoatGloss) * 0.1D + clearcoatGloss * 0.001D);
        randomNormal = shadingFrame.toWorld(randomNormal);
        Vector3D wi = reflect(woWorld, randomNormal);
        wi = shadingFrame.toLocal(wi);
        Vector3D f = eval(wo, wi);
        double pdf = pdf(wo, wi);
        return new BxDFSample(shadingFrame.toWorld(wi), f, pdf, Event.REFLECT, clearcoatGloss == 1.0D);
    }
}
