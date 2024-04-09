package material;

import math.Vector3D;

public class Glass extends Material {
    public Glass(double ambient, double indexOfRefraction) {
        super(new Vector3D(),
                ambient,
                0,
                0,
                0,
                1,
                indexOfRefraction,
                Type.REFLECT_REFRACT,
                false
        );
    }
}
