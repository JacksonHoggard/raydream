package object;

import math.Ray;
import math.Vector2D;
import math.Vector3D;

public interface IObject {

    double intersect(Ray ray);

    Vector3D normalAt(Vector3D point);

    Vector2D mapTexture(Vector3D point);

}
