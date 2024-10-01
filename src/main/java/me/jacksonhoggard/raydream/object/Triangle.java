package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public class Triangle {

    private Vector3D v0, v1, v2;
    private Vector2D t0, t1, t2;
    private Vector3D[] normal;
    private Vector3D normalNotNormal;
    private double d;
    private double area2;
    private Vector3D centroid;
    private Vector3D min;
    private Vector3D max;

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector2D t0, Vector2D t1, Vector2D t2) {
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.t0 = t0;
        this.t1 = t1;
        this.t2 = t2;
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

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector3D n0, Vector3D n1, Vector3D n2, Vector2D t0, Vector2D t1, Vector2D t2) {
        this(v0, v1, v2, t0, t1, t2);
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

    public void calcBarycentric(Vector3D p, Vector3D coords) {
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
            nu = calcTriArea(p.y, p.z, v1.y, v1.z, v2.y, v2.z);
            nv = calcTriArea(p.y, p.z, v2.y, v2.z, v0.y, v0.z);
            ood = 1.d / normalNotNormal.x;
        } else if(y >= x && y >= z) {
            // y is largest, project to the xz plane
            nu = calcTriArea(p.x, p.z, v1.x, v1.z, v2.x, v2.z);
            nv = calcTriArea(p.x, p.z, v2.x, v2.z, v0.x, v0.z);
            ood = 1.d / -normalNotNormal.y;
        } else {
            // z is largest, project to the xy plane
            nu = calcTriArea(p.x, p.y, v1.x, v1.y, v2.x, v2.y);
            nv = calcTriArea(p.x, p.y, v2.x, v2.y, v0.x, v0.y);
            ood = 1.d / normalNotNormal.z;
        }
        double u = nu * ood;
        double v = nv * ood;
        double w = 1.d - u - v;
        coords.set(u, v, w);
    }

    public double intersect(Ray ray) {
        double nDotRDir = normalNotNormal.dot(ray.direction());
        // Compute distance to point hit
        double t = -(normalNotNormal.dot(ray.origin()) + d) / nDotRDir;
        if(t < 0) // point is behind ray
            return -1.0D;

        Vector3D v = new Vector3D();
        calcBarycentric(ray.at(t), v);
        if(v.y >= 0.0d && v.z >= 0.0d && (v.y + v.z) <= 1.0d)
            return t;

        return -1.0D;
    }

    public Vector3D getNormal(Vector3D point) {
        if(normal[0].equals(normal[1]) && normal[1].equals(normal[2])) {
            return normal[0];
        }
        Vector3D temp = new Vector3D();
        calcBarycentric(point, temp);
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

    public Vector2D mapTexture(Vector3D barycentric) {
        double u = barycentric.x;
        double v = barycentric.y;
        double w = barycentric.z;
        return Vector2D.add(Vector2D.mult(u, t0), Vector2D.mult(v, t1)).add(Vector2D.mult(w, t2));
    }

    public void set(Triangle triangle) {
        this.v0 = triangle.v0;
        this.v1 = triangle.v1;
        this.v2 = triangle.v2;
        this.t0 = triangle.t0;
        this.t1 = triangle.t1;
        this.t2 = triangle.t2;
        this.normal = triangle.normal;
        this.normalNotNormal = triangle.normalNotNormal;
        this.d = triangle.d;
        this.area2 = triangle.area2;
        this.centroid = triangle.centroid;
        this.min = triangle.min;
        this.max = triangle.max;
    }
}
