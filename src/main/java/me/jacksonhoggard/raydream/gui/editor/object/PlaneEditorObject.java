package me.jacksonhoggard.raydream.gui.editor.object;

import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.EditorModel;
import me.jacksonhoggard.raydream.gui.editor.model.PlaneModel;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.object.Plane;
import me.jacksonhoggard.raydream.object.Transform;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class PlaneEditorObject extends EditorObject {

    private static final EditorModel planeModel = new PlaneModel(1000, 1000);

    public PlaneEditorObject(EditorObjectMaterial material) {
        super(planeModel, material);
    }

    public PlaneEditorObject() {
        super(
                planeModel,
                new EditorObjectMaterial(
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
        super.show();
        getModelMatrix()[12] = 0;
        getModelMatrix()[14] = 0;
    }

    @Override
    public void draw() {
        glBindVertexArray(planeModel.getVertexArrayId());
        glDrawArrays(GL_TRIANGLES, 0, planeModel.getVertexCount());
        glBindVertexArray(0);
    }

    @Override
    public Object toObject() {
        Transform t = getTransform();
        return new Plane(t.translation().y, t.rotation(), getMaterial().toRayDreamMaterial());
    }

    public static void cleanup() {
        planeModel.remove();
    }
}
