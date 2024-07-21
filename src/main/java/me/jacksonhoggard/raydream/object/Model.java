package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public class Model extends Object {

    private final Mesh mesh;
    private final boolean invertNormals;

    public Model(Transform transform, Material material, Mesh mesh, boolean invertNormals) {
        super(transform, material, mesh.getMin(), mesh.getMax());
        this.mesh = mesh;
        this.invertNormals = invertNormals;
    }

    @Override
    public Hit intersect(Ray ray) {
        double t = Double.MAX_VALUE;
        Vector3D normal = null;
        for(Triangle triangle : mesh.getTriangles()) {
            double distance = triangle.intersect(ray);
            if(distance < t && distance > 0.0D) {
                t = distance;
                normal = triangle.getNormal();
                if(invertNormals) normal = normal.negated();
            }
        }
        if(t < Double.MAX_VALUE)
            return new Hit(this, ray.at(t), transformNormalToWS(normal, getNormalMatrix()), t);
        return new Hit(null, null, null, -1.0D);
    }

    @Override
    public Vector2D mapTexture(Vector3D point) {
        return null;
    }
}
