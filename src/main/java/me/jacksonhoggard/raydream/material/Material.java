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

    private final Vector3D color;
    private final double indexOfRefraction;
    private final double k;
    private double ambient;
    private double lambertian;
    private double specular;
    private double specularExponent;
    private final double metalness;
    private final double roughness; // For matte reflections
    private final Type type;
    private final Texture texture;
    private final BumpMap bumpMap;

    public Material(Vector3D color, double ambient, double lambertian, double specular, double specularExponent, double metalness, double roughness, double indexOfRefraction, double k, Type type, Texture texture, BumpMap bumpMap) {
        this.color = color;
        this.ambient = ambient;
        this.lambertian = lambertian;
        this.specular = specular;
        this.specularExponent = specularExponent;
        this.indexOfRefraction = indexOfRefraction;
        this.k = k;
        this.metalness = metalness;
        this.roughness = roughness;
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
        
        // Account for extinction coefficient if present (for complex metals)
        if (k > 0) {
            double n2 = indexOfRefraction * indexOfRefraction;
            double k2 = k * k;
            F0 = ((n2 + k2) - 2 * indexOfRefraction + 1) / ((n2 + k2) + 2 * indexOfRefraction + 1);
        }
        
        // Schlick's approximation
        return F0 + (1 - F0) * Math.pow(1 - cosTheta, 5);
    }

    public Ray reflectRay(Ray rayIn, Vector3D pointHit, Vector3D normal) {
        Vector3D direction = reflect(rayIn, normal).normalized();
        
        // Apply roughness to create matte reflections
        if (roughness > 0.0) {
            direction = perturbReflectionDirection(direction, roughness);
        }
        
        // Improved bias calculation for reflection rays
        Vector3D origin = Vector3D.add(pointHit, Vector3D.mult(normal, ApplicationConfig.RAY_OFFSET_EPSILON));
        // Add small directional bias to prevent self-intersection
        origin.add(Vector3D.mult(direction, ApplicationConfig.RAY_OFFSET_EPSILON * 0.1));
        return new Ray(origin, direction);
    }
    
    /**
     * Perturbs a reflection direction based on material roughness to create matte reflections.
     * Uses a more physically-based approach with cosine-weighted hemisphere sampling.
     * @param perfectReflection the perfect mirror reflection direction
     * @param roughness the surface roughness (0.0 = perfect mirror, 1.0 = very rough)
     * @return perturbed reflection direction
     */
    private Vector3D perturbReflectionDirection(Vector3D perfectReflection, double roughness) {
        if (roughness <= 0.0) {
            return perfectReflection;
        }
        
        // For very rough surfaces, use cosine-weighted hemisphere sampling around the reflection direction
        // For smoother surfaces, interpolate between perfect reflection and random hemisphere
        if (roughness >= 0.9) {
            // Very rough - use hemisphere sampling around the reflection direction
            return MathUtils.randomHemisphere(perfectReflection);
        } else {
            // Generate a random vector in the hemisphere around the perfect reflection
            Vector3D randomInHemisphere = MathUtils.randomHemisphere(perfectReflection);
            
            // Use a power function to make the roughness falloff more realistic
            double blend = Math.pow(roughness, 0.5); // Square root gives a more natural falloff
            
            // Interpolate between perfect reflection and random direction
            return Vector3D.add(
                Vector3D.mult(perfectReflection, 1.0 - blend),
                Vector3D.mult(randomInHemisphere, blend)
            ).normalized();
        }
    }
    
    public Ray refractRay(Ray rayIn, Vector3D pointHit, Vector3D normal) {
        Vector3D direction = refract(rayIn, normal, indexOfRefraction).normalized();
        // Improved bias calculation for refraction rays
        Vector3D origin = direction.dot(normal) < 0 ?
                Vector3D.sub(pointHit, Vector3D.mult(normal, ApplicationConfig.RAY_OFFSET_EPSILON)) :
                Vector3D.add(pointHit, Vector3D.mult(normal, ApplicationConfig.RAY_OFFSET_EPSILON));
        // Add small directional bias to prevent self-intersection
        origin.add(Vector3D.mult(direction, ApplicationConfig.RAY_OFFSET_EPSILON * 0.1));
        return new Ray(origin, direction);
    }

    public Type getType() {
        return type;
    }

    public Vector3D getColor(Vector2D texCoord) {
        if(texture != null)
            return texture.getColorAt(texCoord.x, texCoord.y);
        return color;
    }

    public double getIndexOfRefraction() {
        return indexOfRefraction;
    }

    public double getMetalness() {
        return metalness;
    }

    public double getRoughness() {
        return roughness;
    }

    public double getAmbient() {
        return ambient;
    }

    public double getLambertian() {
        return lambertian;
    }

    public double getSpecular() {
        return specular;
    }

    public double getSpecularExponent() {
        return specularExponent;
    }

    public double getK() {
        return k;
    }

    public Texture getTexture() {
        return texture;
    }

    public BumpMap getBumpMap() {
        return bumpMap;
    }
}
