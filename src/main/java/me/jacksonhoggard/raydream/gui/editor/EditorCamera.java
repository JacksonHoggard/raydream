package me.jacksonhoggard.raydream.gui.editor;

import imgui.extension.imguizmo.ImGuizmo;
import me.jacksonhoggard.raydream.gui.editor.model.OBJModel;
import me.jacksonhoggard.raydream.gui.editor.window.SettingsWindow;
import me.jacksonhoggard.raydream.math.Matrix4F;
import me.jacksonhoggard.raydream.math.Vector3D;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class EditorCamera {

    private static final OBJModel model;
    static {
        try {
            model = new OBJModel(Paths.get(ClassLoader.getSystemResource("camera.obj").toURI()).toString(), false);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private final Matrix4F viewMatrix;
    private final Matrix4F projectionMatrix;
    private final Matrix4F modelMatrix;

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
        model.create();
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

    public void updateModelMatrix(Vector3D from, Vector3D to) {
        Vector3D direction = new Vector3D(from).sub(to);
        Vector3D directionA = new Vector3D(0, 0, 1);
        Vector3D directionB = new Vector3D(direction).normalize();
        float rotationAngle = (float) Math.acos(directionA.dot(directionB));
        Vector3D rotationAxis = directionA.cross(directionB).normalize();
        float rotX = (float) (rotationAxis.x * rotationAngle);
        float rotY = (float) (rotationAxis.y * rotationAngle);
        float rotZ = (float) (rotationAxis.z * rotationAngle);
        float[] translation = new float[]{(float) from.x, (float) from.y, (float) from.z};
        float[] rotation = new float[]{(float) (rotX * (180 / Math.PI)), (float) (rotY * (180 / Math.PI)), (float) (rotZ * (180 / Math.PI))};
        float[] scale = new float[]{0.03f, 0.03f, 0.03f};
        ImGuizmo.recomposeMatrixFromComponents(modelMatrix.getMatrixArray(), translation, rotation, scale);
    }

    public void draw() {
        glBindVertexArray(model.getVertexArrayId());
        glDrawArrays(GL_TRIANGLES, 0, model.getVertexCount());
        glBindVertexArray(0);
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
}
