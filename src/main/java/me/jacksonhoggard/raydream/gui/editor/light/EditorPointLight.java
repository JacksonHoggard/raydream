package me.jacksonhoggard.raydream.gui.editor.light;

import me.jacksonhoggard.raydream.gui.editor.material.EditorLightMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.BoxModel;
import me.jacksonhoggard.raydream.gui.editor.model.SphereModel;
import me.jacksonhoggard.raydream.light.Light;
import me.jacksonhoggard.raydream.light.PointLight;
import me.jacksonhoggard.raydream.math.Vector3D;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.GL_PRIMITIVE_RESTART;
import static org.lwjgl.opengl.GL31.glPrimitiveRestartIndex;
import static org.lwjgl.opengl.GL43.GL_PRIMITIVE_RESTART_FIXED_INDEX;

public class EditorPointLight extends EditorLight {

    private static final SphereModel sphereModel = new SphereModel();

    public EditorPointLight(EditorLightMaterial material) {
        super(sphereModel, material);
        getModelMatrix()[0] = 0.1f;
        getModelMatrix()[5] = 0.1f;
        getModelMatrix()[10] = 0.1f;
        label.set("Point Light");
    }

    public EditorPointLight() {
        this(new EditorLightMaterial());
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
        return new PointLight(
                getTransform().translation(),
                new Vector3D(
                        getMaterial().getColor()[0],
                        getMaterial().getColor()[1],
                        getMaterial().getColor()[2]
                ),
                getMaterial().getBrightness()
        );
    }

    public static void cleanup() {
        sphereModel.remove();
    }
}
