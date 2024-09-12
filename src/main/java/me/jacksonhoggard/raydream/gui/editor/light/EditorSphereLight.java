package me.jacksonhoggard.raydream.gui.editor.light;

import me.jacksonhoggard.raydream.gui.editor.material.EditorLightMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.SphereModel;
import me.jacksonhoggard.raydream.light.Light;
import me.jacksonhoggard.raydream.light.SphereLight;
import me.jacksonhoggard.raydream.math.Vector3D;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.GL_PRIMITIVE_RESTART;
import static org.lwjgl.opengl.GL31.glPrimitiveRestartIndex;
import static org.lwjgl.opengl.GL43.GL_PRIMITIVE_RESTART_FIXED_INDEX;

public class EditorSphereLight extends EditorLight {

    private static final SphereModel sphereModel = new SphereModel();
    private float radius;

    public EditorSphereLight(EditorLightMaterial material, float radius) {
        super(sphereModel, material);
        this.radius = radius;
        getModelMatrix()[0] = radius;
        getModelMatrix()[5] = radius;
        getModelMatrix()[10] = radius;
    }

    public EditorSphereLight() {
        this(new EditorLightMaterial(), 0.5f);
    }

    @Override
    public void draw() {
        glBindVertexArray(sphereModel.getVertexArrayId());
        glEnable(GL_PRIMITIVE_RESTART);
        glPrimitiveRestartIndex(GL_PRIMITIVE_RESTART_FIXED_INDEX);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, sphereModel.getIndicesBufferId());
        glDrawElements(GL_TRIANGLE_STRIP, sphereModel.getIndicesCount(), GL_UNSIGNED_INT, 0);
        glDisable(GL_PRIMITIVE_RESTART);
        glBindVertexArray(0);
    }

    @Override
    public Light toLight() {
        Vector3D position = getTransform().translation();
        Vector3D color = new Vector3D(
                getMaterial().getColor()[0],
                getMaterial().getColor()[1],
                getMaterial().getColor()[2]
        );
        return new SphereLight(position, color, getMaterial().getBrightness(), radius);
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
