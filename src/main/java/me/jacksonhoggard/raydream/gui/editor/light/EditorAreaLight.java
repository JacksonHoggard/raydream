package me.jacksonhoggard.raydream.gui.editor.light;

import imgui.extension.imguizmo.ImGuizmo;
import me.jacksonhoggard.raydream.gui.editor.material.EditorLightMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.AreaLightModel;
import me.jacksonhoggard.raydream.light.AreaLight;
import me.jacksonhoggard.raydream.light.Light;
import me.jacksonhoggard.raydream.math.Vector3D;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class EditorAreaLight extends EditorLight {

    private static final AreaLightModel model = new AreaLightModel();

    public EditorAreaLight(EditorLightMaterial material) throws IOException {
        super(model, material);
        label.set("Area Light");
    }

    public EditorAreaLight() throws IOException {
        this(new EditorLightMaterial());
    }

    public EditorAreaLight(float[] translation, float[] rotation, float[] scale, EditorLightMaterial material, String label) throws IOException {
        super(model, material);
        ImGuizmo.recomposeMatrixFromComponents(this.getModelMatrix(), translation, rotation, scale);
        this.label.set(label);
    }

    @Override
    public void draw() {
        glBindVertexArray(model.getVertexArrayId());
        glDrawArrays(GL_TRIANGLES, 0, model.getVertexCount());
        glBindVertexArray(0);
    }

    @Override
    public Light toLight() {
        return new AreaLight(getTransform(),
                new Vector3D(
                        getMaterial().getColor()[0],
                        getMaterial().getColor()[1],
                        getMaterial().getColor()[2]
                ),
                getMaterial().getBrightness()
        );
    }

    @Override
    public String toSaveEntry() {
        return "+ light: area\n" +
                "label: " + label.get() + "\n" +
                getTransformSaveEntry() + getMaterial().toSaveEntry() + ";\n";
    }

    public static void cleanup() {
        model.remove();
    }
}
