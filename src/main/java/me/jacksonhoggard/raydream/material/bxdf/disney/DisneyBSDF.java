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
        
        // Get all parameters with safe defaults
        Object metallicObj = parameters != null ? parameters.get("metallic") : null;
        this.metallic = (metallicObj instanceof Double) ? ((Double) metallicObj).doubleValue() : 0.0;
        
        Object subsurfaceObj = parameters != null ? parameters.get("subsurface") : null;
        this.subsurface = (subsurfaceObj instanceof Double) ? ((Double) subsurfaceObj).doubleValue() : 0.0;
        
        Object specularObj = parameters != null ? parameters.get("specular") : null;
        this.specular = (specularObj instanceof Double) ? ((Double) specularObj).doubleValue() : 0.5;
        
        Object roughnessObj = parameters != null ? parameters.get("roughness") : null;
        this.roughness = (roughnessObj instanceof Double) ? ((Double) roughnessObj).doubleValue() : 0.5;
        
        Object specularTintObj = parameters != null ? parameters.get("specularTint") : null;
        this.specularTint = (specularTintObj instanceof Double) ? ((Double) specularTintObj).doubleValue() : 0.0;
        
        Object sheenObj = parameters != null ? parameters.get("sheen") : null;
        this.sheen = (sheenObj instanceof Double) ? ((Double) sheenObj).doubleValue() : 0.0;
        
        Object sheenTintObj = parameters != null ? parameters.get("sheenTint") : null;
        this.sheenTint = (sheenTintObj instanceof Double) ? ((Double) sheenTintObj).doubleValue() : 0.5;
        
        Object clearcoatObj = parameters != null ? parameters.get("clearcoat") : null;
        this.clearcoat = (clearcoatObj instanceof Double) ? ((Double) clearcoatObj).doubleValue() : 0.0;
        
        Object clearcoatGlossObj = parameters != null ? parameters.get("clearcoatGloss") : null;
        this.clearcoatGloss = (clearcoatGlossObj instanceof Double) ? ((Double) clearcoatGlossObj).doubleValue() : 1.0;
        
        Object transmissionObj = parameters != null ? parameters.get("transmission") : null;
        this.transmission = (transmissionObj instanceof Double) ? ((Double) transmissionObj).doubleValue() : 0.0;
        
        Object iorObj = parameters != null ? parameters.get("ior") : null;
        this.ior = (iorObj instanceof Double) ? ((Double) iorObj).doubleValue() : 1.5;
        
        Object thinObj = parameters != null ? parameters.get("thin") : null;
        this.thin = (thinObj instanceof Boolean) ? ((Boolean) thinObj).booleanValue() : false;
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
