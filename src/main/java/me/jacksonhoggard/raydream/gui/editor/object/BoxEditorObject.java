package me.jacksonhoggard.raydream.gui.editor.object;

import imgui.extension.imguizmo.ImGuizmo;
import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.BoxModel;
import me.jacksonhoggard.raydream.gui.editor.model.EditorModel;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Box;
import me.jacksonhoggard.raydream.object.Object;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class BoxEditorObject extends EditorObject {

    private static final EditorModel boxModel = new BoxModel();

    public BoxEditorObject(EditorObjectMaterial material) throws IOException {
        super(boxModel, material);
        label.set("Box");
    }

    public BoxEditorObject() throws IOException {
        this(
                new EditorObjectMaterial(
                        new float[]{1.f, 0.f, 0.f},
                        0.1f,
                        0.4f,
                        0.5f,
                        32,
                        3.638f,
                        0.177f,
                        0.3f,
                        Material.Type.REFLECT,
                        1.f
                )
        );
    }

    public BoxEditorObject(float[] translation, float[] rotation, float[] scale, EditorObjectMaterial material, String label) throws IOException {
        super(boxModel, material);
        ImGuizmo.recomposeMatrixFromComponents(this.getModelMatrix(), translation, rotation, scale);
        this.label.set(label);
    }

    @Override
    public void draw() {
        if(getMaterial().getTexture() != null) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, getMaterial().getTexture().getId());
        }
        glBindVertexArray(boxModel.getVertexArrayId());
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);
    }

    @Override
    public Object toObject() {
        return new Box(getTransform(), new Vector3D(1, 1, 1), getMaterial().toRayDreamMaterial());
    }

    @Override
    public String toSaveEntry(String path) {
        return "+ object: box\n" +
                "label: " + label.get() + "\n" +
                getTransformSaveEntry() + getMaterial().toSaveEntry(path) + ";\n";
    }

    public static void cleanup() {
        boxModel.remove();
    }
}
