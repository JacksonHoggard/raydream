package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public class Triangle {

    private final Vector3D v0, v1, v2;
    private final Vector3D edge0, edge1, edge2;
    private final Vector3D normal;
    private final Vector3D normalNotNormal;
    private final double area2;
    private final Vector3D centroid;
    private final Vector3D min;
    private final Vector3D max;

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.edge0 = Vector3D.sub(v1, v0);
        this.edge1 = Vector3D.sub(v2, v1);
        this.edge2 = Vector3D.sub(v0, v2);
        Vector3D v = Vector3D.sub(v2, v0);
        this.normalNotNormal = edge0.cross(v);
        this.area2 = normalNotNormal.dot(normalNotNormal);
        this.normal = normalNotNormal.normalized();
        this.min = new Vector3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        this.max = new Vector3D(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
        calcMinMax(v0);
        calcMinMax(v1);
        calcMinMax(v2);
        this.centroid = Vector3D.add(this.min, this.max).div(2.0D);
    }

    private void calcMinMax(Vector3D v) {
        min.x = Math.min(min.x, v.x);
        min.y = Math.min(min.y, v.y);
        min.z = Math.min(min.z, v.z);
        max.x = Math.max(max.x, v.x);
        max.y = Math.max(max.y, v.y);
        max.z = Math.max(max.z, v.z);
    }

    public double intersect(Ray ray) {
        double nDotRDir = normalNotNormal.dot(ray.getDirection());
        // Compute distance to point hit
        double d = -normalNotNormal.dot(v0);
        double t = -(normalNotNormal.dot(ray.getOrigin()) + d) / nDotRDir;
        if(t < 0) // point is behind ray
            return -1.0D;
        Vector3D point = ray.at(t); // point hit

        // Edge 0
        Vector3D vp0 = Vector3D.sub(point, v0);
        Vector3D c = edge0.cross(vp0); // vector perpendicular to triangle
        if(normalNotNormal.dot(c) < 0) // point hit is outside the triangle
            return -1.0D;

        // Edge 1
        Vector3D vp1 = Vector3D.sub(point, v1);
        c = edge1.cross(vp1);
        if(normalNotNormal.dot(c) < 0)
            return -1.0D;

        // Edge 2
        Vector3D vp2 = Vector3D.sub(point, v2);
        c = edge2.cross(vp2);
        if(normalNotNormal.dot(c) < 0)
            return -1.0D;

        return t;
    }

    public Vector3D getNormal() {
        return normal;
    }

    public Vector3D getCentroid() {
        return centroid;
    }

    public Vector3D getMin() {
        return min;
    }

    public Vector3D getMax() {
        return max;
    }

    public Vector2D mapTexture(Vector3D point) {
        Vector3D vp1 = Vector3D.sub(point, v1);
        Vector3D perp = edge1.cross(vp1);
        double u = normalNotNormal.dot(perp) / area2;
        Vector3D vp2 = Vector3D.sub(point, v2);
        perp = edge2.cross(vp2);
        double v = normalNotNormal.dot(perp) / area2;
        return new Vector2D(u, v);
    }
}
