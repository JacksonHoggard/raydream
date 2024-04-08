package material;

import math.Vector3D;

public class Matte extends Material {
    public Matte(Vector3D albedo, double ambient, double lambertian, double specular, double specularExponent) {
        super(
                albedo,
                ambient,
                0.6D,
                specular,
                specularExponent,
                1,
                0,
                Type.OTHER
        );
    }
}
