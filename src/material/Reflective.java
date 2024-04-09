package material;

import math.Vector3D;

public class Reflective extends Material {
    public Reflective(Vector3D albedo, double ambient, double lambertian, double metalness) {
        super(albedo,
                ambient,
                lambertian,
                0.8D,
                25,
                metalness,
                1,
                Type.REFLECT,
                true
        );
    }
}
