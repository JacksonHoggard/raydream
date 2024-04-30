package object;

import material.Material;
import math.Ray;
import math.Vector2D;
import math.Vector3D;
import math.Vector4D;

public class Model extends Object {

    private final Mesh mesh;
    private final boolean invertNormals;

    public Model(Transform transform, Material material, Mesh mesh, boolean invertNormals) {
        super(transform, material);
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
        return new Hit(this, null, null, -1.0D);
    }

    @Override
    public Vector2D mapTexture(Vector3D point) {
        return null;
    }
}
