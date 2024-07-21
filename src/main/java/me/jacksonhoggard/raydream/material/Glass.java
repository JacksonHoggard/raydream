package me.jacksonhoggard.raydream.material;

import me.jacksonhoggard.raydream.math.Vector3D;

public class Glass extends Material {
    public Glass(double ambient, double indexOfRefraction) {
        super(new Vector3D(),
                ambient,
                0.3D,
                0.6D,
                100,
                0,
                indexOfRefraction,
                0,
                Type.REFLECT_REFRACT,
                false
        );
    }
}
