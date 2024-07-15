package light;

import math.Ray;
import math.Vector3D;

public class PointLight extends Light {

    public PointLight(Vector3D position, Vector3D color, double brightness) {
        super(position, color, brightness);
    }

    @Override
    public double intersect(Ray ray) {
        double t;
        Vector3D origin = ray.getOrigin();
        Vector3D direction = ray.getDirection();
        t = Vector3D.sub(origin, getPosition()).dot(direction.negated());
        if (t > 0)
            return t;
        return -1;
    }

    @Override
    public Vector3D pointOnLight(int i, int j, int cols, int rows) {
        return getPosition();
    }
}
