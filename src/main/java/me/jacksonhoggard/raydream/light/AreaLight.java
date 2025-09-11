package me.jacksonhoggard.raydream.light;

import me.jacksonhoggard.raydream.math.*;
import me.jacksonhoggard.raydream.object.Hit;
import me.jacksonhoggard.raydream.object.Transform;
import me.jacksonhoggard.raydream.object.Triangle;
import me.jacksonhoggard.raydream.util.MathUtils;
import me.jacksonhoggard.raydream.util.Util;

public class AreaLight extends Light {
    private final Triangle t0, t1;

    public AreaLight(Transform transform, Vector3D color, double brightness) {
        super(transform, new Vector3D(-0.5, -0.5, 1e-5D), new Vector3D(0.5, 0.5, 1e-5D), color, brightness);
        this.getPosition().set(MathUtils.transformPointToWS(new Vector3D(), getTransformMatrix()));
        this.t0 = new Triangle(
                new Vector3D(-0.5, 0.5, 0),
                new Vector3D(-0.5, -0.5, 0),
                new Vector3D(0.5, -0.5, 0),
                null,
                null,
                null
        );
        this.t1 = new Triangle(
                new Vector3D(0.5, -0.5, 0),
                new Vector3D(0.5, 0.5, 0),
                new Vector3D(-0.5, 0.5, 0),
                null,
                null,
                null
        );
        double length = MathUtils.transformPointToWS(new Vector3D(-0.5, -0.5, 0), getTransformMatrix())
                                 .distance(MathUtils.transformPointToWS(new Vector3D(-0.5, 0.5, 0), getTransformMatrix()));
        double width = MathUtils.transformPointToWS(new Vector3D(-0.5, -0.5, 0), getTransformMatrix())
                                .distance(MathUtils.transformPointToWS(new Vector3D(0.5, -0.5, 0), getTransformMatrix()));
        this.area = width * length;
    }

    @Override
    public Hit intersect(Ray ray) {
        double t0Dist = t0.intersect(ray);
        double t1Dist = t1.intersect(ray);
        if(t0Dist <= 0.0D && t1Dist > 0.0D) {
            Vector3D point = ray.at(t1Dist);
            return new Hit(this, t1, MathUtils.transformPointToWS(point, getTransformMatrix()), t1.getNormal(point), null, t1Dist);
        }
        if(t1Dist <= 0.0D && t0Dist > 0.0D) {
            Vector3D point = ray.at(t0Dist);
            return new Hit(this, t0, MathUtils.transformPointToWS(point, getTransformMatrix()), t0.getNormal(point), null, t0Dist);
        }
        if(t0Dist > 0.0D && t1Dist > 0.0D) {
            double tDist = Math.min(t0Dist, t1Dist);
            Vector3D point = ray.at(tDist);
            if(tDist == t0Dist) {
                return new Hit(this, t0, MathUtils.transformPointToWS(point, getTransformMatrix()), t0.getNormal(point), null, t0Dist);
            } else {
                return new Hit(this, t1, MathUtils.transformPointToWS(point, getTransformMatrix()), t1.getNormal(point), null, t1Dist);
            }
        }
        return new Hit(null, null, null, null, null, -1.0D);
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
    public double samplingWeight() {
        return MathUtils.luminance(getColor()) * area * Math.PI;
    }

    @Override
    public boolean isDelta() {
        return false;
    }

    @Override
    public Vector3D pointOnLight(int i, int j, int cols, int rows) {
        double gridSizeX = 1.0D / cols;
        double gridSizeY = 1.0D / rows;
        double x = -0.5D + (i * gridSizeX) + Util.randomRange(0, gridSizeX);
        double y = -0.5D + (j * gridSizeY) + Util.randomRange(0, gridSizeY);
        Vector3D pointOS = new Vector3D(x, y, 0);
        return MathUtils.transformPointToWS(pointOS, getTransformMatrix());
    }

    @Override
    public Vector3D closestPoint(Vector3D point) {
        Vector3D pointOS = MathUtils.transformPointToOS(point, getInverseTransformMatrix());
        Vector3D closestPoint = new Vector3D(
            Math.min(Math.max(pointOS.x, -0.5D), 0.5D),
            Math.min(Math.max(pointOS.y, -0.5D), 0.5D),
            0
        );
        closestPoint = MathUtils.transformPointToWS(closestPoint, getTransformMatrix());
        return closestPoint;
    }

    @Override
    public Vector3D normalAt(Vector3D point) {
        Vector3D pointOS = MathUtils.transformPointToOS(point, getInverseTransformMatrix());
        Vector4D normalOS = new Vector4D(0, 0, pointOS.z, 0).normalize();
        if(normalOS.length() == 0.0D)
            normalOS = new Vector4D(0, 0, 1, 0);
        normalOS = normalOS.mult(getTransformMatrix()).normalized();
        return new Vector3D(normalOS.x, normalOS.y, normalOS.z);
    }
}
