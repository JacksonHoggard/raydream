package material;

import math.Vector3D;

public class ColoredGlass extends Material {
    public ColoredGlass(Vector3D albedo, double ambient, double indexOfRefraction) {
        super(
                albedo,
                ambient,
                0,
                0,
                0,
                1,
                indexOfRefraction,
                Type.REFLECT_REFRACT,
                true
        );
    }
}
