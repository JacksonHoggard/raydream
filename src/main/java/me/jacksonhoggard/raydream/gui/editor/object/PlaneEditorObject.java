package me.jacksonhoggard.raydream.gui.editor.object;

import imgui.ImGui;
import me.jacksonhoggard.raydream.gui.editor.material.EditorMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.EditorModel;
import me.jacksonhoggard.raydream.gui.editor.model.PlaneModel;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.object.Object;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class PlaneEditorObject extends EditorObject {

    private static final EditorModel planeModel = new PlaneModel();

    public PlaneEditorObject(EditorMaterial material) {
        super(planeModel, material);
    }

    public PlaneEditorObject() {
        super(
                planeModel,
                new EditorMaterial(
                        new float[] {0.8f, 0.8f, 0.8f},
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
    public void show() {
        ImGui.pushID(id);

        if(ImGui.selectable(label, id == selected)) {
            selected = id;
        }

        ImGui.popID();
    }

    @Override
    public void draw() {
        glBindVertexArray(planeModel.getVertexArrayId());
        glDrawArrays(GL_TRIANGLES, 0, planeModel.getVertexCount());
        glBindVertexArray(0);
    }

    @Override
    public Object toObject() {
        return null;
    }
}
