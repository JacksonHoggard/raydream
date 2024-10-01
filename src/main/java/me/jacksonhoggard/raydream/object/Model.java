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
            return new Hit(this, ray.at(t), transformNormalToWS(triangle.getNormal(ray.at(t)), getNormalMatrix()), mapTexture(triangle, barycentric), t);
        }
        return new Hit(null, null, null, null, -1.0D);
    }

    private Vector2D mapTexture(Triangle triangle, Vector3D barycentric) {
        return triangle.mapTexture(barycentric);
    }

    @Override
    public Vector2D mapTexture(Vector3D point) {
        return null;
    }

    public Mesh getMesh() {
        return mesh;
    }
}
