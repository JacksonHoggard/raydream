package object;

import math.Ray;
import math.Vector3D;

public interface IObject {

    double intersect(Ray ray);

    Vector3D normalAt(Vector3D point);

}
