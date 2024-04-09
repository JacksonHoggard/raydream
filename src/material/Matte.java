package material;

import math.Vector3D;

public class Matte extends Material {
    public Matte(Vector3D albedo, double ambient, double lambertian, double specular, double specularExponent) {
        super(
                albedo,
                ambient,
                lambertian,
                specular,
                specularExponent,
                0.5D,
                1.5D,
                Type.OTHER,
                true
        );
    }
}
