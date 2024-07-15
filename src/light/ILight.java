package light;

import math.Ray;
import math.Vector3D;

public interface ILight {
    double intersect(Ray ray);

    Vector3D pointOnLight(int i, int j, int cols, int rows);
}
