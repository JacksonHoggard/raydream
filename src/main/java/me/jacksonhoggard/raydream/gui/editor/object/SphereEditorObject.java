package me.jacksonhoggard.raydream.gui.editor.object;

import imgui.extension.imguizmo.ImGuizmo;
import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.SphereModel;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.object.Sphere;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.GL_PRIMITIVE_RESTART;
import static org.lwjgl.opengl.GL31.glPrimitiveRestartIndex;
import static org.lwjgl.opengl.GL43.GL_PRIMITIVE_RESTART_FIXED_INDEX;

public class SphereEditorObject extends EditorObject {

    private static final SphereModel sphereModel = new SphereModel();

    public SphereEditorObject(EditorObjectMaterial material) throws IOException {
        super(sphereModel, material);
        label.set("Sphere");
    }

    public SphereEditorObject() throws IOException {
        this(
                new EditorObjectMaterial(
                        new float[]{0.f, 1.f, 1.f},
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

    public SphereEditorObject(float[] translation, float[] rotation, float[] scale, EditorObjectMaterial material, String label) throws IOException {
        super(sphereModel, material);
        ImGuizmo.recomposeMatrixFromComponents(this.getModelMatrix(), translation, rotation, scale);
        this.label.set(label);
    }

    @Override
    public void draw() {
        if(getMaterial().getTexture() != null) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, getMaterial().getTexture().getId());
        }
        glBindVertexArray(sphereModel.getVertexArrayId());
        glEnable(GL_PRIMITIVE_RESTART);
        glPrimitiveRestartIndex(GL_PRIMITIVE_RESTART_FIXED_INDEX);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, sphereModel.getIndicesBufferId());
        glDrawElements(GL_TRIANGLE_STRIP, sphereModel.getIndicesCount(), GL_UNSIGNED_INT, 0);
        glDisable(GL_PRIMITIVE_RESTART);
        glBindVertexArray(0);
    }

    @Override
    public Object toObject() {
        return new Sphere(getTransform(), 1.0D, getMaterial().toRayDreamMaterial());
    }

    @Override
    public String toSaveEntry(String path) {
        return "+ object: sphere\n" +
                "label: " + label.get() + "\n" +
                getTransformSaveEntry() + getMaterial().toSaveEntry(path) + ";\n";
    }

    public static void cleanup() {
        sphereModel.remove();
    }
}
