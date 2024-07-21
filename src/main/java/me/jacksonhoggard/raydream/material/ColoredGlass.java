package me.jacksonhoggard.raydream.material;

import me.jacksonhoggard.raydream.math.Vector3D;

public class ColoredGlass extends Material {
    public ColoredGlass(Vector3D color, double ambient, double indexOfRefraction) {
        super(
                color,
                ambient,
                0.3D,
                0.6D,
                100,
                0,
                indexOfRefraction,
                0,
                Type.REFLECT_REFRACT,
                true
        );
    }
}
