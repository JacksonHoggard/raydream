package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public class Model extends Object {

    private final Mesh mesh;
    private final BVHTriangle bvh;

    public Model(Transform transform, Material material, Mesh mesh) {
        super(transform, material, mesh.min(), mesh.max());
        this.mesh = mesh;
        bvh = new BVHTriangle(this.mesh.triangles());
    }

    @Override
    public Hit intersect(Ray ray) {
        Triangle triangle = new Triangle(new Vector3D(), new Vector3D(), new Vector3D(), new Vector2D(), new Vector2D(), new Vector2D());
        double t = bvh.intersect(ray, mesh.triangles(), triangle);
        if(t < Double.MAX_VALUE) {
            Vector3D barycentric = new Vector3D();
            triangle.calcBarycentric(ray.at(t), barycentric);
            return new Hit(this, triangle, ray.at(t), triangle.getNormal(ray.at(t)), mapTexture(triangle, barycentric), t);
        }
        return new Hit(null, null, null, null, null, -1.0D);
    }

    public boolean intersectShadowRay(Ray ray, double lightDistance) {
        return bvh.intersectShadowRay(ray, mesh.triangles(), lightDistance);
    }

    private Vector2D mapTexture(Triangle triangle, Vector3D barycentric) {
        return triangle.mapTexture(barycentric);
    }

    @Override
    public Vector2D mapTexture(Vector3D point) {
        return null;
    }

    @Override
    public Vector3D calcTangent(Vector3D normal) {
        return null;
    }

    @Override
    public Vector3D calcBitangent(Vector3D normal, Vector3D tangent) {
        return null;
    }

    public Mesh getMesh() {
        return mesh;
    }
}
