package me.jacksonhoggard.raydream.material;

import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

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
    private final Type type;
    private final Texture texture;
    private final BumpMap bumpMap;

    public Material(Vector3D color, double ambient, double lambertian, double specular, double specularExponent, double metalness, double indexOfRefraction, double k, Type type, Texture texture, BumpMap bumpMap) {
        this.color = color;
        this.ambient = ambient;
        this.lambertian = lambertian;
        this.specular = specular;
        this.specularExponent = specularExponent;
        this.indexOfRefraction = indexOfRefraction;
        this.k = k;
        this.metalness = metalness;
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

    // Fresnel for metals
    public double fresnelMetal(Ray rayIn, Vector3D normal) {
        Vector3D v = rayIn.direction().normalized();
        double cosi = Math.abs(Math.clamp(v.dot(normal), -1, 1));
        double top = Math.pow(indexOfRefraction - 1, 2) + ((4 * indexOfRefraction) * Math.pow(1 - cosi, 5)) + k*k;
        double bottom = Math.pow(indexOfRefraction + 1, 2) + k*k;
        return top / bottom;
    }

    public Ray reflectRay(Ray rayIn, Vector3D pointHit, Vector3D normal) {
        Vector3D direction = reflect(rayIn, normal).normalized();
        Vector3D origin = direction.dot(normal) < 0 ?
                Vector3D.sub(pointHit, Vector3D.mult(normal, 0.00000001D)) :
                Vector3D.add(pointHit, Vector3D.mult(normal, 0.00000001D));
        return new Ray(origin, direction);
    }

    public Ray refractRay(Ray rayIn, Vector3D pointHit, Vector3D normal) {
        Vector3D direction = refract(rayIn, normal, indexOfRefraction).normalized();
        Vector3D origin = direction.dot(normal) < 0 ?
                Vector3D.sub(pointHit, Vector3D.mult(normal, 0.00000001D)) :
                Vector3D.add(pointHit, Vector3D.mult(normal, 0.00000001D));
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
