package material;

import math.Ray;
import math.Vector3D;

public abstract class Material {

    public enum Type {
        REFLECT,
        REFLECT_REFRACT,
        OTHER
    }

    private final Vector3D albedo;
    private final double indexOfRefraction;
    private double ambient;
    private double lambertian;
    private double specular;
    private double specularExponent;
    private final double metalness;
    private final Type type;

    public Material(Vector3D albedo, double ambient, double lambertian, double specular, double specularExponent, double metalness, double indexOfRefraction, Type type) {
        this.albedo = albedo;
        this.ambient = ambient;
        this.lambertian = lambertian;
        this.specular = specular;
        this.specularExponent = specularExponent;
        this.indexOfRefraction = indexOfRefraction;
        this.metalness = metalness;
        this.type = type;
    }

    public static Vector3D reflect(Ray rayIn, Vector3D normal) {
        Vector3D v = rayIn.getDirection().normalized();
        return v.sub(Vector3D.mult(normal, 2*v.dot(normal)));
    }

    public static Vector3D refract(Ray rayIn, Vector3D normal, double ratio) {
        Vector3D v = rayIn.getDirection().normalized();
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

    public double fresnel(Ray rayIn, Vector3D normal) {
        Vector3D v = rayIn.getDirection();
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

    public Vector3D getAlbedo() {
        return albedo;
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

}
