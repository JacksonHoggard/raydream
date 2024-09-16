package me.jacksonhoggard.raydream.gui.editor.object;

import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.OBJModel;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.object.Model;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.util.Util;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class OBJEditorObject extends EditorObject {

    private final OBJModel modelSmooth;
    private final OBJModel modelFlat;
    private boolean smooth;

    public OBJEditorObject(String path, EditorObjectMaterial material) {
        super(new OBJModel(path, true), material);
        modelSmooth = (OBJModel) this.getModel();
        modelFlat = new OBJModel(modelSmooth.getPath(), false);
        modelFlat.create();
        this.smooth = true;
    }

    public OBJEditorObject(String path) {
        super(new OBJModel(path, true), new EditorObjectMaterial(
                new float[] {0.f, 1.f, 0.f},
                0.1f,
                0.4f,
                0.5f,
                32,
                3.638f,
                0.177f,
                0.3f,
                Material.Type.REFLECT
        ));
        modelSmooth = (OBJModel) this.getModel();
        modelFlat = new OBJModel(modelSmooth.getPath(), false);
        modelFlat.create();
        this.smooth = true;
    }

    @Override
    public void draw() {
        if(!smooth) {
            glBindVertexArray(modelFlat.getVertexArrayId());
            glDrawArrays(GL_TRIANGLES, 0, modelFlat.getVertexCount());
        } else {
            glBindVertexArray(modelSmooth.getVertexArrayId());
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, modelSmooth.getIndicesBufferId());
            glDrawElements(GL_TRIANGLES, modelFlat.getIndicesCount(), GL_UNSIGNED_INT, 0);
        }
        glBindVertexArray(0);
    }

    @Override
    public Object toObject() {
        return new Model(getTransform(), getMaterial().toRayDreamMaterial(), Util.loadOBJ(((OBJModel) getModel()).getPath()), false);
    }

    public void setSmooth(boolean smooth) {
        this.smooth = smooth;
    }

    public boolean isSmooth() {
        return smooth;
    }

    @Override
    public void remove() {
        modelSmooth.remove();
        modelFlat.remove();
    }
}
