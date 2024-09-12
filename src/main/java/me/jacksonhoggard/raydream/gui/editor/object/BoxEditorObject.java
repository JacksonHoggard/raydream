package me.jacksonhoggard.raydream.gui.editor.object;

import imgui.ImGui;
import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.BoxModel;
import me.jacksonhoggard.raydream.gui.editor.model.EditorModel;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Box;
import me.jacksonhoggard.raydream.object.Object;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class BoxEditorObject extends EditorObject {

    private static final EditorModel boxModel = new BoxModel();

    public BoxEditorObject(EditorObjectMaterial material) {
        super(boxModel, material);
    }

    public BoxEditorObject() {
        super(
                boxModel,
                new EditorObjectMaterial(
                        new float[]{1.f, 0.f, 0.f},
                        0.1f,
                        0.4f,
                        0.5f,
                        32,
                        3.638f,
                        0.177f,
                        0.3f,
                        Material.Type.REFLECT
                )
        );
    }

    @Override
    public void draw() {
        glBindVertexArray(boxModel.getVertexArrayId());
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);
    }

    @Override
    public Object toObject() {
        return new Box(getTransform(), new Vector3D(1, 1, 1), getMaterial().toRayDreamMaterial());
    }
}
