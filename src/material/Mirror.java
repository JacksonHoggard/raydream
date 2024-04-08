package material;

import math.Vector3D;

public class Mirror extends Material {
    public Mirror(Vector3D albedo) {
        super(albedo,
                0.1D,
                0.5D,
                0.8D,
                25,
                0,
                2,
                Type.REFLECT);
    }
}
