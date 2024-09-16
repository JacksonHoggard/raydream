package me.jacksonhoggard.raydream.gui.editor.light;

import me.jacksonhoggard.raydream.gui.editor.material.EditorLightMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.BoxModel;
import me.jacksonhoggard.raydream.light.Light;
import me.jacksonhoggard.raydream.light.PointLight;
import me.jacksonhoggard.raydream.math.Vector3D;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class EditorPointLight extends EditorLight {

    private static final BoxModel boxModel = new BoxModel();

    public EditorPointLight(EditorLightMaterial material) {
        super(boxModel, material);
        getModelMatrix()[0] = 0.1f;
        getModelMatrix()[5] = 0.1f;
        getModelMatrix()[10] = 0.1f;
    }

    public EditorPointLight() {
        this(new EditorLightMaterial());
    }

    @Override
    public void draw() {
        glBindVertexArray(boxModel.getVertexArrayId());
        glDrawArrays(GL_TRIANGLES, 0, 36);
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
        boxModel.remove();
    }
}
