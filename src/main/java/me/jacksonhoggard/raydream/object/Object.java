package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Matrix4D;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.math.Vector4D;

public abstract class Object implements IObject {

    private final Transform transform;
    private final Material material;
    private final Matrix4D inverseTransformMatrix;
    private final Matrix4D normalMatrix;
    private final Vector3D centroid;
    private final Vector3D min;
    private final Vector3D max;

    public Object(Transform transform, Material material, Vector3D min, Vector3D max) {
        this.transform = transform;
        this.material = material;
        float[] temp = new float[]{
                1.f, 0, 0, 0,
                0, 1.f, 0, 0,
                0, 0, 1.f, 0,
                0, 0, 0, 1.f
        };
        Matrix4D transformMatrix = composeModelMatrix(transform);

        this.inverseTransformMatrix = new Matrix4D(transformMatrix.inverse().getMatrixArray());
        this.normalMatrix = inverseTransformMatrix.transpose();
        this.min = new Vector3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        this.max = new Vector3D(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
        Vector3D[] aabb = {
                min,
                new Vector3D(min.x, max.y, min.z),
                new Vector3D(max.x, min.y, min.z),
                new Vector3D(max.x, max.y, min.z),
                new Vector3D(min.x, min.y, max.z),
                new Vector3D(min.x, max.y, max.z),
                new Vector3D(max.x, min.y, max.z),
                max
        };
        for(Vector3D v : aabb) {
            v = transformPointToOS(v, transformMatrix);
            this.min.x = Math.min(this.min.x, v.x);
            this.min.y = Math.min(this.min.y, v.y);
            this.min.z = Math.min(this.min.z, v.z);
            this.max.x = Math.max(this.max.x, v.x);
            this.max.y = Math.max(this.max.y, v.y);
            this.max.z = Math.max(this.max.z, v.z);
        }
        this.centroid = Vector3D.add(this.min, this.max).div(2.0D);
    }

    public static Matrix4D composeModelMatrix(Transform transform) {
        Matrix4D matrix;
        double[] translation, rotation, scale;
        translation = new double[]{
                transform.translation().x,
                transform.translation().y,
                transform.translation().z
        };
        rotation = new double[]{
                transform.rotation().x,
                transform.rotation().y,
                transform.rotation().z
        };
        scale = new double[]{
                transform.scale().x,
                transform.scale().y,
                transform.scale().z
        };

        Matrix4D[] rot = new Matrix4D[3];
        Vector4D[] dirs = new Vector4D[]{
                new Vector4D(1, 0, 0, 0),
                new Vector4D(0, 1, 0, 0),
                new Vector4D(0, 0, 1, 0)
        };
        for (int i = 0; i < 3; i++)
        {
            float angle = (float) (rotation[i] * Math.PI / 180.F);
            Vector4D axis = dirs[i];
            float length2 = (float) Math.pow(dirs[i].length(), 2);
            if (length2 < 1e-5)
            {
                rot[i] = new Matrix4D(
                        1, 0, 0, 0,
                        0, 1, 0, 0,
                        0, 0, 1, 0,
                        0, 0, 0, 1
                );
                continue;
            }

            Vector4D n = Vector4D.mult(axis, (1.f / Math.sqrt(length2)));
            float s = (float) Math.sin(angle);
            float c = (float) Math.cos(angle);
            float k = 1.f - c;

            float xx = (float) (n.x * n.x * k + c);
            float yy = (float) (n.y * n.y * k + c);
            float zz = (float) (n.z * n.z * k + c);
            float xy = (float) (n.x * n.y * k);
            float yz = (float) (n.y * n.z * k);
            float zx = (float) (n.z * n.x * k);
            float xs = (float) (n.x * s);
            float ys = (float) (n.y * s);
            float zs = (float) (n.z * s);

            rot[i] = new Matrix4D(
                    xx, xy + zs, zx - ys, 0.f,
                    xy - zs, yy, yz + xs, 0.f,
                    zx + ys, yz - xs, zz, 0.f,
                    0.f, 0.f, 0.f, 1.f
            );
        }

        matrix = rot[0].mult(rot[1]).mult(rot[2]);

        float[] validScale = new float[3];
        for (int i = 0; i < 3; i++)
        {
            if (Math.abs(scale[i]) < 1e-5)
            {
                validScale[i] = 0.001f;
            }
            else
            {
                validScale[i] = (float) scale[i];
            }
        }
        Vector4D right = new Vector4D(
                matrix.getMatrixArray()[0],
                matrix.getMatrixArray()[1],
                matrix.getMatrixArray()[2],
                matrix.getMatrixArray()[3]
        );
        Vector4D up = new Vector4D(
                matrix.getMatrixArray()[4],
                matrix.getMatrixArray()[5],
                matrix.getMatrixArray()[6],
                matrix.getMatrixArray()[7]
        );
        Vector4D dir = new Vector4D(
                matrix.getMatrixArray()[8],
                matrix.getMatrixArray()[9],
                matrix.getMatrixArray()[10],
                matrix.getMatrixArray()[11]
        );
        Vector4D position = new Vector4D(
                translation[0],
                translation[1],
                translation[2],
                1.f
        );
        right.mult(validScale[0]);
        up.mult(validScale[1]);
        dir.mult(validScale[2]);
        matrix = new Matrix4D(
                right.x, right.y, right.z, right.w,
                up.x, up.y, up.z, up.w,
                dir.x, dir.y, dir.z, dir.w,
                position.x, position.y, position.z, position.w
        );
        return matrix.transpose();
    }

    public static Vector3D transformNormalToWS(Vector3D normal, Matrix4D normalMatrix) {
        Vector4D normalWS = new Vector4D(normal.x, normal.y, normal.z, 0);
        normalWS = normalWS.mult(normalMatrix);
        return new Vector3D(normalWS.x, normalWS.y, normalWS.z).normalize();
    }

    public static Vector3D transformPointToOS(Vector3D point, Matrix4D inverseTransformMatrix) {
        Vector4D pointWS = new Vector4D(point.x, point.y, point.z, 1);
        Vector4D pointOS = pointWS.mult(inverseTransformMatrix);
        return new Vector3D(pointOS.x, pointOS.y, pointOS.z);
    }
    public Material getMaterial() {
        return material;
    }

    public Transform getTransform() {
        return transform;
    }

    public Matrix4D getInverseTransformMatrix() {
        return inverseTransformMatrix;
    }

    public Matrix4D getNormalMatrix() {
        return normalMatrix;
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
}
