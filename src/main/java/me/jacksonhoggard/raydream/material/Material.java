package me.jacksonhoggard.raydream.material;

import me.jacksonhoggard.raydream.config.ApplicationConfig;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.util.MathUtils;

public class Material {

    public enum Type {
        REFLECT,
        REFLECT_REFRACT,
        OTHER
    }

    // Disney BRDF
    private final Vector3D albedo;
    private final double subsurface;
    private final double metallic;
    private final Vector3D specular;
    private final double specularTint;
    private final double specularTransmission;
    private final double roughness;
    private final double anisotropic;
    private final double sheen;
    private final double sheenTint;
    private final double clearcoat;
    private final double clearcoatGloss;

    // Other parameters
    private final double indexOfRefraction;
    private final Type type;
    private final Texture texture;
    private final BumpMap bumpMap;

    public Material(
        Vector3D albedo,
        double subsurface,
        double metallic,
        Vector3D specular,
        double specularTint,
        double specularTransmission,
        double roughness,
        double anisotropic,
        double sheen,
        double sheenTint,
        double clearcoat,
        double clearcoatGloss,
        double indexOfRefraction,
        Type type,
        Texture texture,
        BumpMap bumpMap
    ) {
        this.albedo = albedo;
        this.subsurface = subsurface;
        this.metallic = metallic;
        this.specular = specular;
        this.specularTint = specularTint;
        this.specularTransmission = specularTransmission;
        this.roughness = roughness;
        this.anisotropic = anisotropic;
        this.sheen = sheen;
        this.sheenTint = sheenTint;
        this.clearcoat = clearcoat;
        this.clearcoatGloss = clearcoatGloss;
        this.indexOfRefraction = indexOfRefraction;
        this.type = type;
        this.texture = texture;
        this.bumpMap = bumpMap;
    }

    public static Vector3D reflect(Ray rayIn, Vector3D normal) {
        Vector3D v = rayIn.direction().normalized();
        return v.sub((Vector3D.mult(normal, 2*v.dot(normal))));
    }

    public static Vector3D refract(Ray rayIn, Vector3D normal, double ratio) {
        Vector3D v = rayIn.direction().normalized();
        double cosi = Math.clamp(v.dot(normal), -1, 1);
        double etai = 1;
        double etat = ratio;
        Vector3D n = new Vector3D(normal);
        if(cosi < 0) {
            cosi = -cosi;
        } else {
            double temp = etai;
            etai = etat;
            etat = temp;
            n.negate();
        }
        double eta = etai / etat;
        double k = 1 - eta * eta * (1 - cosi * cosi);
        return k < 0 ? new Vector3D() : v.mult(eta).add(n.mult(eta * cosi - Math.sqrt(k)));
    }

    // Fresnel for dielectrics
    public double fresnelDielectric(Ray rayIn, Vector3D normal) {
        Vector3D v = rayIn.direction().normalized();
        double cosi = Math.clamp(v.dot(normal), -1, 1);
        double etai = 1;
        double etat = indexOfRefraction;
        if(cosi > 0) {
            double temp = etai;
            etai = etat;
            etat = temp;
        }
        double sint = etai / etat * Math.sqrt(Math.max(0.0D, 1 - cosi * cosi));
        if(sint >= 1)
            return 1.0D;
        double cost = Math.sqrt(Math.max(0.0D, 1 - sint * sint));
        cosi = Math.abs(cosi);
        double Rs = ((etat * cosi) - (etai * cost)) / ((etat * cosi) + (etai * cost));
        double Rp = ((etai * cosi) - (etat * cost)) / ((etai * cosi) + (etat * cost));
        return (Rs * Rs + Rp * Rp) / 2;
    }

    // Fresnel for metals using Schlick's approximation with extinction coefficient
    public double fresnelMetal(Ray rayIn, Vector3D normal) {
        Vector3D v = rayIn.direction().normalized();
        double cosTheta = Math.abs(Math.clamp(v.dot(normal), -1, 1));
        
        // Calculate F0 (reflectance at normal incidence)
        double F0 = Math.pow((indexOfRefraction - 1) / (indexOfRefraction + 1), 2);
        
        // Schlick's approximation
        return F0 + (1 - F0) * Math.pow(1 - cosTheta, 5);
    }

    public Ray reflectRay(Ray rayIn, Vector3D pointHit, Vector3D normal) {
        Vector3D direction = reflect(rayIn, normal).normalized();
        // Apply roughness to create matte reflections
        if (roughness > 0.0) {
            direction = perturbReflectionDirection(direction, normal, roughness);
        }
        Vector3D origin = direction.dot(normal) < 0.0D ?
                Vector3D.sub(pointHit, Vector3D.mult(normal, ApplicationConfig.RAY_OFFSET_EPSILON)) :
                Vector3D.add(pointHit, Vector3D.mult(normal, ApplicationConfig.RAY_OFFSET_EPSILON));
        return new Ray(origin, direction);
    }
    
    /**
     * Perturbs a reflection direction based on material roughness to create matte reflections.
     * Uses a more physically-based approach with cosine-weighted hemisphere sampling.
     * @param perfectReflection the perfect mirror reflection direction
     * @param roughness the surface roughness (0.0 = perfect mirror, 1.0 = very rough)
     * @return perturbed reflection direction
     */
    private Vector3D perturbReflectionDirection(Vector3D perfectReflection, Vector3D normal, double roughness) {
        // Use a more conservative roughness mapping to avoid excessive noise
        double effectiveRoughness = roughness * roughness; // Square the roughness for more gradual falloff
        // Generate a random vector in the hemisphere around the surface normal
        Vector3D randomInHemisphere = MathUtils.randomHemisphere(normal);
        // Interpolate between perfect reflection and random direction
        return MathUtils.lerp(perfectReflection, randomInHemisphere, effectiveRoughness).normalize();
    }
    
    public Ray refractRay(Ray rayIn, Vector3D pointHit, Vector3D normal) {
        Vector3D direction = refract(rayIn, normal, indexOfRefraction).normalize();
        Vector3D origin = direction.dot(normal) < 0 ?
                Vector3D.sub(pointHit, Vector3D.mult(normal, ApplicationConfig.RAY_OFFSET_EPSILON)) :
                Vector3D.add(pointHit, Vector3D.mult(normal, ApplicationConfig.RAY_OFFSET_EPSILON));
        return new Ray(origin, direction);
    }

    public Type getType() {
        return type;
    }

    public Vector3D getAlbedo(Vector2D texCoord) {
        if(texture != null)
            return texture.getColorAt(texCoord.x, texCoord.y);
        return albedo;
    }

    public double getSubsurface() {
        return subsurface;
    }

    public double getMetallic() {
        return metallic;
    }

    public Vector3D getSpecular() {
        return specular;
    }

    public double getSpecularTint() {
        return specularTint;
    }

    public double getSpecularTransmission() {
        return specularTransmission;
    }

    public double getRoughness() {
        return roughness;
    }

    public double getAnisotropic() {
        return anisotropic;
    }

    public double getSheen() {
        return sheen;
    }

    public double getSheenTint() {
        return sheenTint;
    }

    public double getClearcoat() {
        return clearcoat;
    }

    public double getClearcoatGloss() {
        return clearcoatGloss;
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
}
