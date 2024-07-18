package light;

import math.Ray;
import math.Vector3D;

public class PointLight extends Light {

    public PointLight(Vector3D position, Vector3D color, double brightness) {
        super(position, color, brightness);
    }

    @Override
    public double intersect(Ray ray) {
        Vector3D oc = Vector3D.sub(ray.getOrigin(), getPosition());
        double a = ray.getDirection().dot(ray.getDirection());
        double b = 2.0D * oc.dot(ray.getDirection());
        double c = oc.dot(oc) - 0.1D * 0.1D;
        double discriminant = b*b - 4*a*c;
        if(discriminant < 0.0D) {
            return -1;
        }
        return (-b - Math.sqrt(discriminant)) / (2*a);
    }

    @Override
    public Vector3D pointOnLight(int i, int j, int cols, int rows) {
        return getPosition();
    }
}
