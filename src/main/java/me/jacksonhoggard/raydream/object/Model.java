package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public class Model extends Object {

    private final Mesh mesh;
    private final BVHTriangle bvh;

    public Model(Transform transform, Material material, Mesh mesh) {
        super(transform, material, mesh.getMin(), mesh.getMax());
        this.mesh = mesh;
        bvh = new BVHTriangle(this.mesh.getTriangles());
    }

    @Override
    public Hit intersect(Ray ray) {
        Vector3D normal = new Vector3D();
        double t = bvh.intersect(ray, mesh.getTriangles(), normal);
        if(t < Double.MAX_VALUE) {
            return new Hit(this, ray.at(t), transformNormalToWS(normal, getNormalMatrix()), t);
        }
        return new Hit(null, null, null, -1.0D);
    }

    @Override
    public Vector2D mapTexture(Vector3D point) {
        return null;
    }

    public Mesh getMesh() {
        return mesh;
    }
}
