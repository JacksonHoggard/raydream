package material.texture;

import math.Vector2D;
import math.Vector3D;
import object.Object;

public abstract class Texture {

    public Vector3D patternAt(Object object, Vector3D point) {
        Vector2D map = object.mapTexture(point);
        return uvPatternAt(map.x, map.y);
    }

    protected Vector3D uvPatternAt(double u, double v) {
        return new Vector3D();
    }

}
