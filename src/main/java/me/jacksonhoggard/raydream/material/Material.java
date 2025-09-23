package me.jacksonhoggard.raydream.material;

import java.util.HashMap;

import me.jacksonhoggard.raydream.material.bxdf.BxDF;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public class Material<T extends BxDF> {

    private final Vector3D albedo;
    private final Vector3D emittance;
    private final double indexOfRefraction;
    private final Texture texture;
    private final BumpMap bumpMap;
    private final HashMap<String, Object> parameters;
    private final Class<T> bxdfClass;

    private Material(Class<T> bxdfClass,
        Vector3D albedo,
        Vector3D emittance,
        double indexOfRefraction,
        Texture texture,
        BumpMap bumpMap,
        HashMap<String, Object> parameters
    ) {
        this.albedo = new Vector3D(
            Math.clamp(albedo.x, 0, 1),
            Math.clamp(albedo.y, 0, 1),
            Math.clamp(albedo.z, 0, 1)
        );
        this.emittance = new Vector3D(
            Math.clamp(emittance.x, 0, 1),
            Math.clamp(emittance.y, 0, 1),
            Math.clamp(emittance.z, 0, 1)
        );
        this.indexOfRefraction = indexOfRefraction;
        this.texture = texture;
        this.bumpMap = bumpMap;
        this.parameters = parameters;
        this.bxdfClass = bxdfClass;
    }

    public Vector3D getAlbedo(Vector2D texCoord) {
        if(texture != null && texCoord != null)
            return texture.getColorAt(texCoord.x, texCoord.y);
        return albedo;
    }

    public Vector3D getEmittance() {
        return emittance;
    }

    public double getIndexOfRefraction() {
        return indexOfRefraction;
    }

    public Texture getTexture() {
        return texture;
    }

    public BumpMap getBumpMap() {
        return bumpMap;
    }

    public boolean isEmissive() {
        return !emittance.equals(Vector3D.ZERO);
    }

    public T createBxDF(Vector3D ng, Vector3D ns, Vector2D uv) {
        T bxdf = null;
        try {
            // Check for null parameters before creating the BxDF
            if (ng == null) {
                System.err.println("ERROR: ng (geometric normal) is null in createBxDF");
                return null;
            }
            if (ns == null) {
                System.err.println("ERROR: ns (shading normal) is null in createBxDF");
                return null;
            }
            if (uv == null) {
                System.err.println("ERROR: uv (texture coordinates) is null in createBxDF");
                return null;
            }
            if (bxdfClass == null) {
                System.err.println("ERROR: bxdfClass is null in createBxDF");
                return null;
            }
            
            Vector3D albedo = getAlbedo(uv);
            if (albedo == null) {
                System.err.println("ERROR: albedo returned null from getAlbedo() in createBxDF");
                return null;
            }
            
            // parameters can be null, but let's log it for debugging
            if (parameters == null) {
                System.err.println("WARNING: parameters is null in createBxDF (this may be okay)");
            }
            
            bxdf = (T) bxdfClass.getConstructor(Vector3D.class, Vector3D.class, Vector3D.class, HashMap.class)
            .newInstance(ng, ns, albedo, parameters);
        } catch (Exception e) {
            System.err.println("ERROR in createBxDF: " + e.getMessage());
            System.err.println("Parameters: ng=" + ng + ", ns=" + ns + ", uv=" + uv + ", bxdfClass=" + bxdfClass);
            e.printStackTrace();
        }
        return bxdf;
    }

    public static <T extends BxDF> Material<T> of(
        Class<T> type,
        Vector3D albedo,
        Vector3D emittance,
        double indexOfRefraction,
        Texture texture,
        BumpMap bumpMap,
        HashMap<String, Object> parameters
    ) {
        return new Material<T>(
            type,
            albedo,
            emittance,
            indexOfRefraction,
            texture,
            bumpMap,
            parameters
        );
    }
}
