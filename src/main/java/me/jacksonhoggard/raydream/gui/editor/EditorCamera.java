package me.jacksonhoggard.raydream.gui.editor;

import imgui.extension.imguizmo.ImGuizmo;
import me.jacksonhoggard.raydream.gui.editor.model.OBJModel;
import me.jacksonhoggard.raydream.math.Matrix4F;
import me.jacksonhoggard.raydream.math.Vector3D;

import java.io.IOException;

public class EditorCamera {

    private static final OBJModel model;
    static {
            model = new OBJModel("camera.obj", ClassLoader.getSystemResourceAsStream("camera.obj"));
    }

    private final Matrix4F viewMatrix;
    private final Matrix4F projectionMatrix;
    private final Matrix4F modelMatrix;

    private float fov;
    private float aspect;
    private float near;
    private float far;
    private final Vector3D lookFrom;
    private final Vector3D lookAt;
    private final Vector3D up;

    public EditorCamera(float fov, float aspect, float near, float far) {
        viewMatrix = new Matrix4F(
                1.f, 0.f, 0.f, 0.f,
                0.f, 1.f, 0.f, 0.f,
                0.f, 0.f, 1.f, 0.f,
                0.f, 0.f, 0.f, 1.f
        );
        modelMatrix = new Matrix4F(
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
        lookFrom = new Vector3D(0, 1, 2);
        lookAt = new Vector3D(0, 0, 0);
        up = new Vector3D(0, 1, 0);
        try {
            model.create();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateProjection() {
        float t, b, r, l;
        t = (float) (near * Math.tan((fov * Math.PI / 180.0f) / 2));
        r = t * aspect;
        b = -t;
        l = -r;
        projectionMatrix.set(new Matrix4F(
                2*near/(r-l), 0.f, 0.f, 0.f,
                0.0f, 2*near/(t-b), 0.f, 0.f,
                (r+l)/(r-l), (t+b)/(t-b), -(far+near) / (far-near), -1.f,
                0.f, 0.f, -2*far*near/(far-near), 0.f
        ));
    }

    public void updateViewMatrix() {
        Vector3D x;
        Vector3D y;
        Vector3D z;
        Vector3D temp = new Vector3D();

        temp.x = lookFrom.x - lookAt.x;
        temp.y = lookFrom.y - lookAt.y;
        temp.z = lookFrom.z - lookAt.z;
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
                (float) -x.dot(lookFrom), (float) -y.dot(lookFrom), (float) -z.dot(lookFrom), 1.f
        );
    }

    public void updateModelMatrix() {
        Vector3D direction = new Vector3D(lookFrom).sub(lookAt);
        Vector3D directionA = new Vector3D(0, 0, 1);
        Vector3D directionB = new Vector3D(direction).normalize();
        float rotationAngle = (float) Math.acos(directionA.dot(directionB));
        Vector3D rotationAxis = directionA.cross(directionB).normalize();
        float rotX = (float) (rotationAxis.x * rotationAngle);
        float rotY = (float) (rotationAxis.y * rotationAngle);
        float rotZ = (float) (rotationAxis.z * rotationAngle);
        float[] translation = new float[]{(float) lookFrom.x, (float) lookFrom.y, (float) lookFrom.z};
        float[] rotation = new float[]{(float) (rotX * (180 / Math.PI)), (float) (rotY * (180 / Math.PI)), (float) (rotZ * (180 / Math.PI))};
        float[] scale = new float[]{0.03f, 0.03f, 0.03f};
        ImGuizmo.recomposeMatrixFromComponents(modelMatrix.getMatrixArray(), translation, rotation, scale);
    }

    public void draw() {
        model.getMeshes().getFirst().draw();
    }

    public Matrix4F getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4F getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4F getModelMatrix() {
        return modelMatrix;
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

    public Vector3D getLookAt() {
        return lookAt;
    }

    public Vector3D getLookFrom() {
        return lookFrom;
    }

    public void setLookFrom(float x, float y, float z) {
        lookFrom.set(x, y, z);
    }

    public void setLookAt(float x, float y, float z) {
        lookAt.set(x, y, z);
    }

    public Vector3D getUp() {
        return up;
    }

    public void setUp(float x, float y, float z) {
        up.set(x, y, z);
    }

    public static OBJModel getModel() {
        return model;
    }
}
