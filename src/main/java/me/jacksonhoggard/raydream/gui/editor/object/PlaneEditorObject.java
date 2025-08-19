package me.jacksonhoggard.raydream.gui.editor.object;

import imgui.extension.imguizmo.ImGuizmo;
import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.EditorModel;
import me.jacksonhoggard.raydream.gui.editor.model.PlaneModel;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.object.Plane;
import me.jacksonhoggard.raydream.object.Transform;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class PlaneEditorObject extends EditorObject {

    private static final EditorModel planeModel = new PlaneModel(200, 200);

    public PlaneEditorObject(EditorObjectMaterial material) throws IOException {
        super(planeModel, material);
        label.set("Plane");
    }

    public PlaneEditorObject() throws IOException {
        this(
                new EditorObjectMaterial(
                        new float[] {0.6f, 0.4f, 0.2f}, // Wood-like brown color
                        0.1f,
                        0.6f, // Higher diffuse for wood
                        0.3f, // Lower specular for wood
                        16,   // Lower specular exponent for wood
                        3.638f,
                        0.177f,
                        0.1f, // Low metalness for wood
                        0.7f, // High roughness for matte reflection (like hardwood)
                        Material.Type.REFLECT,
                        1.f
                )
        );
    }

    public PlaneEditorObject(float[] translation, float[] rotation, float[] scale, EditorObjectMaterial material, String label) throws IOException {
        super(planeModel, material);
        ImGuizmo.recomposeMatrixFromComponents(this.getModelMatrix(), translation, rotation, scale);
        this.label.set(label);
    }

    @Override
    public void show() {
        super.show();
        getModelMatrix()[12] = 0;
        getModelMatrix()[14] = 0;
    }

    @Override
    public void draw() {
        if(getMaterial().getTexture() != null) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, getMaterial().getTexture().getId());
        }
        glBindVertexArray(planeModel.getVertexArrayId());
        glDrawArrays(GL_TRIANGLES, 0, planeModel.getVertexCount());
        glBindVertexArray(0);
    }

    @Override
    public Object toObject() {
        Transform t = getTransform();
        return new Plane(t.translation().y, t.rotation(), getMaterial().toRayDreamMaterial());
    }

    @Override
    public String toSaveEntry(String path) {
        return "+ object: plane\n" +
                "label: " + label.get() + "\n" +
                getTransformSaveEntry() + getMaterial().toSaveEntry(path) + ";\n";
    }

    public static void cleanup() {
        planeModel.remove();
    }
}
