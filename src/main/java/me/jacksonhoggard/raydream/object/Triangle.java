package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public class Triangle {

    private final Vector3D v0, v1, v2;
    private final Vector3D edge0, edge1, edge2;
    private final Vector3D[] normal;
    private final Vector3D normalNotNormal;
    private final double d;
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
//        Vector3D v = Vector3D.sub(v2, v0);
        this.normalNotNormal = Vector3D.sub(v1, v0).cross(Vector3D.sub(v2, v0));
        this.d = -normalNotNormal.dot(v0);
        this.area2 = normalNotNormal.dot(normalNotNormal);
        this.normal = new Vector3D[3];
        this.normal[0] = normalNotNormal.normalized();
        this.normal[1] = normal[0];
        this.normal[2] = normal[1];
        this.min = new Vector3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        this.max = new Vector3D(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
        calcMinMax(v0);
        calcMinMax(v1);
        calcMinMax(v2);
        this.centroid = Vector3D.add(this.min, this.max).div(2.0D);
    }

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector3D n0, Vector3D n1, Vector3D n2) {
        this(v0, v1, v2);
        normal[0] = n0;
        normal[1] = n1;
        normal[2] = n2;
    }

    private void calcMinMax(Vector3D v) {
        min.x = Math.min(min.x, v.x);
        min.y = Math.min(min.y, v.y);
        min.z = Math.min(min.z, v.z);
        max.x = Math.max(max.x, v.x);
        max.y = Math.max(max.y, v.y);
        max.z = Math.max(max.z, v.z);
    }

    public static double calcTriArea(double x1, double y1, double x2, double y2, double x3, double y3) {
        return (x1 - x2) * (y2 - y3) - (x2 - x3) * (y1 - y2);
    }

    private Vector3D calcBarycentric(Vector3D a, Vector3D b, Vector3D c, Vector3D p) {
        // Unnormalized triangle normal
        // Nominators and one-over-denominator for u and v ratios
        double nu, nv, ood;
        // Absolute components for determining projection plane
        double x = Math.abs(normalNotNormal.x);
        double y = Math.abs(normalNotNormal.y);
        double z = Math.abs(normalNotNormal.z);

        // Compute areas in plane of largest projection
        if(x >= y && x >= z) {
            // x is largest, project to the yz plane
            nu = calcTriArea(p.y, p.z, b.y, b.z, c.y, c.z);
            nv = calcTriArea(p.y, p.z, c.y, c.z, a.y, a.z);
            ood = 1.d / normalNotNormal.x;
        } else if(y >= x && y >= z) {
            // y is largest, project to the xz plane
            nu = calcTriArea(p.x, p.z, b.x, b.z, c.x, c.z);
            nv = calcTriArea(p.x, p.z, c.x, c.z, a.x, a.z);
            ood = 1.d / -normalNotNormal.y;
        } else {
            // z is largest, project to the xy plane
            nu = calcTriArea(p.x, p.y, b.x, b.y, c.x, c.y);
            nv = calcTriArea(p.x, p.y, c.x, c.y, a.x, a.y);
            ood = 1.d / normalNotNormal.z;
        }
        double u = nu * ood;
        double v = nv * ood;
        double w = 1.d - u - v;
        return new Vector3D(u, v, w);
    }

    public double intersect(Ray ray) {
        double nDotRDir = normalNotNormal.dot(ray.getDirection());
        // Compute distance to point hit
        double t = -(normalNotNormal.dot(ray.getOrigin()) + d) / nDotRDir;
        if(t < 0) // point is behind ray
            return -1.0D;

        Vector3D v = calcBarycentric(v0, v1, v2, ray.at(t));
        if(v.y >= 0.0d && v.z >= 0.0d && (v.y + v.z) <= 1.0d)
            return t;

        return -1.0D;
    }

    public Vector3D getNormal(Vector3D point) {
        if(normal[0].equals(normal[1]) && normal[1].equals(normal[2])) {
            return normal[0];
        }
        Vector3D temp = calcBarycentric(v0, v1, v2, point);
        double u = temp.x;
        double v = temp.y;
        double w = temp.z;
        return Vector3D.add(Vector3D.mult(u, normal[0]), Vector3D.mult(v, normal[1])).add(Vector3D.mult(w, normal[2])).normalized();
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
