package light;

import math.Ray;
import math.Vector3D;

public record Light(Vector3D position, Vector3D color, double brightness) {
    public double intersect(Ray ray) {
        double t;
        Vector3D origin = ray.getOrigin();
        Vector3D direction = ray.getDirection();
        t = Vector3D.sub(origin, position).dot(direction.negated());
        if (t > 0)
            return t;
        return -1;
    }
}
