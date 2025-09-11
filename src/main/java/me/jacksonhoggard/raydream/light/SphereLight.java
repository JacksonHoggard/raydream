package me.jacksonhoggard.raydream.light;

import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Hit;
import me.jacksonhoggard.raydream.util.MathUtils;
import me.jacksonhoggard.raydream.util.Util;

public class SphereLight extends Light {

    private final double radius;

    public SphereLight(Vector3D position, Vector3D color, double brightness, double radius) {
        super(position, color, brightness);
        this.radius = radius;
        this.area = 4.0D * Math.PI * radius * radius;
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
        return new Hit(this, null, MathUtils.transformPointToWS(point, getTransformMatrix()), normalAt(point), null, (-b - Math.sqrt(discriminant)) / (2*a));
    }

    @Override
    public double samplingWeight() {
        return MathUtils.luminance(getColor()) * area * Math.PI;
    }

    @Override
    public LightSample sampleLight(Vector3D p) {
        Vector3D y = pointOnLight(0, 0, 1, 1);
        Vector3D to = Vector3D.sub(y, p);
        double dist = to.length();
        if(dist <= 0.0D)
            return null;
        Vector3D wi = to.div(dist);

        // Cosine on the light's side (outgoing on light = -wi)
        double cosOnLight = normalAt(y).dot(wi.negated());
        cosOnLight = Math.max(0.0D, Math.abs(cosOnLight));

        // Convert area pdf to solid angle pdf
        double A = area;
        if(A <= 0.0D)
            return null;
        if(cosOnLight == 0.0D)
            return null;
        double pdfW = (dist * dist) / (A * cosOnLight);

        // Radiance along -wi leaving the light
        Vector3D Le = Vector3D.mult(getColor(), getBrightness());

        return new LightSample(wi, Le, dist, pdfW, false);
    }

    @Override
    public boolean isDelta() {
        return false;
    }

    @Override
    public Vector3D pointOnLight(int i, int j, int cols, int rows) {
        Vector3D random = Util.randomUnitVector();
        return Vector3D.add(getPosition(), random.mult(radius - 0.00001D));
    }

    @Override
    public Vector3D closestPoint(Vector3D point) {
        Vector3D dirToPoint = Vector3D.sub(point, getPosition()).normalize();
        Vector3D closestPoint = Vector3D.add(getPosition(), Vector3D.mult(dirToPoint, radius));
        return closestPoint;
    }

    @Override
    public Vector3D normalAt(Vector3D point) {
        Vector3D dirToPoint = Vector3D.sub(point, getPosition()).normalize();
        return dirToPoint;
    }

    public double getRadius() {
        return radius;
    }
}
