package material;

import math.Vector3D;

public class Glass extends Material {
    public Glass(Vector3D albedo, double ambient, double lambertian, double specular, double specularExponent, double indexOfRefraction) {
        super(
                albedo,
                ambient,
                lambertian,
                specular,
                specularExponent,
                0D,
                indexOfRefraction,
                Type.REFLECT_REFRACT
        );
    }
}
