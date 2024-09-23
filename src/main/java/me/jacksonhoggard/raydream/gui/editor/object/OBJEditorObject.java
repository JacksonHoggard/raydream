package me.jacksonhoggard.raydream.gui.editor.object;

import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.OBJModel;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.math.Vector3F;
import me.jacksonhoggard.raydream.object.Mesh;
import me.jacksonhoggard.raydream.object.Model;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.object.Triangle;

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
        Vector3D[] vertices = new Vector3D[modelSmooth.getVertexCount()];
        Vector3D[] normals = new Vector3D[modelSmooth.getVertexCount()];
        Triangle[] triangles = new Triangle[modelSmooth.getIndicesCount() / 3];
        Vector3D min = new Vector3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        Vector3D max = new Vector3D(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

        int i = 0;
        for(int j = 0; j < vertices.length; j++) {
            vertices[j] = new Vector3D(
                    modelSmooth.getVertices()[i],
                    modelSmooth.getVertices()[i + 1],
                    modelSmooth.getVertices()[i + 2]
            );
            normals[j] = new Vector3D(
                    modelSmooth.getVertices()[i + 3],
                    modelSmooth.getVertices()[i + 4],
                    modelSmooth.getVertices()[i + 5]
            );
            min.x = Math.min(min.x, vertices[j].x);
            min.y = Math.min(min.y, vertices[j].y);
            min.z = Math.min(min.z, vertices[j].z);
            max.x = Math.max(max.x, vertices[j].x);
            max.y = Math.max(max.y, vertices[j].y);
            max.z = Math.max(max.z, vertices[j].z);
            i+=6;
        }

        i = 0;
        for(int t = 0; t < triangles.length; t++) {
            if(smooth) {
                triangles[t] = new Triangle(
                        vertices[modelSmooth.getIndices()[i]],
                        vertices[modelSmooth.getIndices()[i+1]],
                        vertices[modelSmooth.getIndices()[i+2]],
                        normals[modelSmooth.getIndices()[i]],
                        normals[modelSmooth.getIndices()[i+1]],
                        normals[modelSmooth.getIndices()[i+2]]
                );
            } else {
                triangles[t] = new Triangle(
                        vertices[modelSmooth.getIndices()[i]],
                        vertices[modelSmooth.getIndices()[i+1]],
                        vertices[modelSmooth.getIndices()[i+2]]
                );
            }
            i+=3;
        }

        Mesh mesh = new Mesh(modelSmooth.getPath(), triangles, min, max, false);
        return new Model(getTransform(), getMaterial().toRayDreamMaterial(), mesh, false);
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
