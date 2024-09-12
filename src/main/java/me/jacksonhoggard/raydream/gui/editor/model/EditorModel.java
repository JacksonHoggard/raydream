package me.jacksonhoggard.raydream.gui.editor.model;

public abstract class EditorModel implements IEditorModel {

    protected int vertexArrayId;
    protected int vertexBufferId;
    protected int indicesBufferId;
    protected int vertexCount;
    protected int indicesCount;
    protected float[] vertices;
    protected int[] indices;
    protected boolean created;

    public EditorModel() {
        created = false;
    }

    public float[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public int getVertexArrayId() {
        return vertexArrayId;
    }

    public int getVertexBufferId() {
        return vertexBufferId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public int getIndicesBufferId() {
        return indicesBufferId;
    }

    public int getIndicesCount() {
        return indicesCount;
    }
}
