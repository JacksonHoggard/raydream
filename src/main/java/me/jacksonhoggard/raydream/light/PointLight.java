package me.jacksonhoggard.raydream.light;

import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Hit;
import me.jacksonhoggard.raydream.util.MathUtils;

public class PointLight extends Light {

    public PointLight(Vector3D position, Vector3D color, double brightness) {
        super(position, color, brightness);
    }

    @Override
    public Hit intersect(Ray ray) {
        Vector3D oc = Vector3D.sub(ray.origin(), getPosition());
        double a = ray.direction().dot(ray.direction());
        double b = 2.0D * oc.dot(ray.direction());
        double c = oc.dot(oc) - 0.1D * 0.1D;
        double discriminant = b*b - 4*a*c;
        if(discriminant < 0.0D) {
            return new Hit(null, null, null, null, null, -1.0D);
        }
        Vector3D point = ray.at((-b - Math.sqrt(discriminant)) / (2*a));
        return new Hit(this, null, point, normalAt(point), null, (-b - Math.sqrt(discriminant)) / (2*a));
    }

    @Override
    public double samplingWeight() {
        return MathUtils.luminance(getColor());
    }

    @Override
    public LightSample sampleLight(Vector3D p) {
        Vector3D to = Vector3D.sub(getPosition(), p);
        double dist = to.length();
        if(dist <= 0.0D)
            return null;
        Vector3D wi = to.div(dist);

        // For point lights, radiance arriving at p is intensity / dist^2
        Vector3D Li = Vector3D.div(Vector3D.mult(getColor(), getBrightness()), dist * dist);

        return new LightSample(wi, Li, dist, 1.0D, true);
    }

    @Override
    public boolean isDelta() {
        return true;
    }

    @Override
    public Vector3D pointOnLight(int i, int j, int cols, int rows) {
        return getPosition();
    }

    @Override
    public Vector3D closestPoint(Vector3D point) {
        return getPosition();
    }

    @Override
    public Vector3D normalAt(Vector3D point) {
        return Vector3D.sub(point, getPosition()).normalized();
    }
}
