package me.jacksonhoggard.raydream.gui.editor;

import me.jacksonhoggard.raydream.math.Matrix4F;
import me.jacksonhoggard.raydream.math.Vector3D;

public class EditorCamera {

    private final Matrix4F viewMatrix;
    private final Matrix4F projectionMatrix;

    private float fov;
    private float aspect;
    private float near;
    private float far;

    public EditorCamera(float fov, float aspect, float near, float far) {
        viewMatrix = new Matrix4F(
                1.f, 0.f, 0.f, 0.f,
                0.f, 1.f, 0.f, 0.f,
                0.f, 0.f, 1.f, 0.f,
                0.f, 0.f, 0.f, 1.f
        );
        projectionMatrix = new Matrix4F(new float[16]);
        this.fov = fov;
        this.aspect = aspect;
        this.near = near;
        this.far = far;
    }

    public void updateProjection() {
        float ymax, xmax;
        ymax = (float) (near * Math.tan(fov * Math.PI / 180.0f));
        xmax = ymax * aspect;
        float temp = 2.0f * near;
        float temp2 = xmax - -xmax;
        float temp3 = ymax - -ymax;
        float temp4 = far - near;
        projectionMatrix.set(new Matrix4F(
                temp / temp2, 0.f, 0.f, 0.f,
                0.0f, temp / temp3, 0.f, 0.f,
                (xmax + -xmax) / temp2, (ymax + -ymax) / temp3, (-far - near) / temp4, -1.f,
                0.f, 0.f, (-temp * far) / temp4, 0.f
        ));
    }

    public void updateViewMatrix(Vector3D eye, Vector3D at, Vector3D up) {
        Vector3D x;
        Vector3D y;
        Vector3D z;
        Vector3D temp = new Vector3D();

        temp.x = eye.x - at.x;
        temp.y = eye.y - at.y;
        temp.z = eye.z - at.z;
        z = temp.normalized();
        y = up.normalized();

        temp = y.cross(z);
        x = temp.normalized();

        temp = z.cross(x);
        y = temp.normalized();

        viewMatrix.set(
                (float) x.x, (float) y.x, (float) z.x, 0.f,
                (float) x.y, (float) y.y, (float) z.y, 0.f,
                (float) x.z, (float) y.z, (float) z.z, 0.f,
                (float) -x.dot(eye), (float) -y.dot(eye), (float) -z.dot(eye), 1.f
        );
    }

    public Matrix4F getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4F getProjectionMatrix() {
        return projectionMatrix;
    }

    public float getFov() {
        return fov;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }

    public float getAspect() {
        return aspect;
    }

    public void setAspect(float aspect) {
        this.aspect = aspect;
    }

    public float getNear() {
        return near;
    }

    public void setNear(float near) {
        this.near = near;
    }

    public float getFar() {
        return far;
    }

    public void setFar(float far) {
        this.far = far;
    }
}
