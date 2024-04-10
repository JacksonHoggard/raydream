package material;

import math.Vector3D;

public class Reflective extends Material {
    public Reflective(Vector3D color, double ambient, double lambertian, double specular, double specularExponent, double metalness, double indexOfRefraction, double k) {
        super(color,
                ambient,
                lambertian,
                specular,
                specularExponent,
                metalness,
                indexOfRefraction,
                k,
                Type.REFLECT,
                true
        );
    }
}
