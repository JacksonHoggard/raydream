package object;

import material.Material;
import math.Ray;
import math.Vector2D;
import math.Vector3D;
import math.Vector4D;

public class Model extends Object {

    private final Mesh mesh;
    private Vector3D normal;
    private final boolean invertNormals;

    public Model(Transform transform, Material material, Mesh mesh, boolean invertNormals) {
        super(transform, material);
        this.mesh = mesh;
        this.invertNormals = invertNormals;
    }

    @Override
    public double intersect(Ray ray) {
        double t = Double.MAX_VALUE;
        for(Triangle triangle : mesh.getTriangles()) {
            double distance = triangle.intersect(ray);
            if(distance < t && distance > 0.0D) {
                t = distance;
                normal = triangle.getNormal();
                if(invertNormals) normal = normal.negated();
            }
        }
        if(t < Double.MAX_VALUE)
            return t;
        return -1.0D;
    }

    @Override
    public Vector3D normalAt(Vector3D point) {
        Vector4D normalWS = new Vector4D(normal.x, normal.y, normal.z, 0);
        normalWS = normalWS.mult(getNormalMatrix());
        return new Vector3D(normalWS.x, normalWS.y, normalWS.z);
    }

    @Override
    public Vector2D mapTexture(Vector3D point) {
        return null;
    }
}
