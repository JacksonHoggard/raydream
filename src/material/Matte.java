package material;

import math.Vector3D;

public class Matte extends Material {
    public Matte(Vector3D color, double ambient, double lambertian, double specular, double specularExponent) {
        super(
                color,
                ambient,
                lambertian,
                specular,
                specularExponent,
                0.5D,
                0,
                0,
                Type.OTHER,
                true
        );
    }
}
