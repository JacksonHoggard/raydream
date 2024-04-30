package object;

import math.Ray;
import math.Vector2D;
import math.Vector3D;

public interface IObject {

    Hit intersect(Ray ray);

    Vector2D mapTexture(Vector3D point);

}
