package me.jacksonhoggard.raydream.material.bxdf.disney;

import java.util.HashMap;

import me.jacksonhoggard.raydream.material.bxdf.BSDF;
import me.jacksonhoggard.raydream.math.Vector3D;

public class DisneyBSDF extends BSDF {

    // --- Material parameters (Disney 2012/2015 set, simplified) ---
    protected final double metallic; // 0..1
    protected final double subsurface; // 0..1 (used lightly in diffuse)
    protected final double specular; // 0..1 (~index via 0..1 knob)
    protected final double roughness; // 0..1
    protected final double specularTint; // 0..1
    protected final double sheen; // 0..1
    protected final double sheenTint; // 0..1
    protected final double clearcoat; // 0..1
    protected final double clearcoatGloss; // 0..1
    protected final double transmission; // 0..1 (dielectric glass)
    protected final double ior; // index of refraction for transmission
    protected final boolean thin; // if true: thin sheet approximation for transmission (optional)

    public DisneyBSDF(
            Vector3D ng, Vector3D ns,
            Vector3D baseColor,
            HashMap<String, Object> parameters
    ) {
        super(ng, ns, baseColor, parameters);
        this.metallic = ((Double) parameters.get("metallic")).doubleValue();
        this.subsurface = ((Double) parameters.get("subsurface")).doubleValue();
        this.specular = ((Double) parameters.get("specular")).doubleValue();
        this.roughness = ((Double) parameters.get("roughness")).doubleValue();
        this.specularTint = ((Double) parameters.get("specularTint")).doubleValue();
        this.sheen = ((Double) parameters.get("sheen")).doubleValue();
        this.sheenTint = ((Double) parameters.get("sheenTint")).doubleValue();
        this.clearcoat = ((Double) parameters.get("clearcoat")).doubleValue();
        this.clearcoatGloss = ((Double) parameters.get("clearcoatGloss")).doubleValue();
        this.transmission = ((Double) parameters.get("transmission")).doubleValue();
        this.ior = ((Double) parameters.get("ior")).doubleValue();
        this.thin = ((Boolean) parameters.get("thin")).booleanValue();
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

    private boolean isMetal() {
        return metallic >= 0.999;
    }
}
