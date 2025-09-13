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
        if(texture != null)
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
            bxdf = (T) bxdfClass.getConstructor(Vector3D.class, Vector3D.class, Vector3D.class, HashMap.class)
            .newInstance(ng, ns, getAlbedo(uv), parameters);
        } catch (Exception e) {
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
