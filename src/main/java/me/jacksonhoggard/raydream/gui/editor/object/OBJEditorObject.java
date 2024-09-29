package me.jacksonhoggard.raydream.gui.editor.object;

import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.OBJModel;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Mesh;
import me.jacksonhoggard.raydream.object.Model;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.object.Triangle;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
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
        if(getMaterial().getTexture() != null) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, getMaterial().getTexture().getId());
        }
        if(!smooth) {
            glBindVertexArray(modelFlat.getVertexArrayId());
            glDrawArrays(GL_TRIANGLES, 0, modelFlat.getVertexCount());
        } else {
            glBindVertexArray(modelSmooth.getVertexArrayId());
            glDrawArrays(GL_TRIANGLES, 0, modelSmooth.getVertexCount());
        }
        glBindVertexArray(0);
    }

    @Override
    public Object toObject() {
        OBJModel model = smooth ? modelSmooth : modelFlat;
        Vector3D[] vertices = new Vector3D[model.getVertexCount()];
        Vector3D[] normals = new Vector3D[model.getVertexCount()];
        Vector2D[] texCoords = new Vector2D[model.getVertexCount()];
        Triangle[] triangles = new Triangle[model.getIndicesCount() / 3];
        Vector3D min = new Vector3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        Vector3D max = new Vector3D(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

        int i = 0;
        for(int j = 0; j < vertices.length; j++) {
            vertices[j] = new Vector3D(
                    model.getVertices()[i],
                    model.getVertices()[i + 1],
                    model.getVertices()[i + 2]
            );
            normals[j] = new Vector3D(
                    model.getVertices()[i + 3],
                    model.getVertices()[i + 4],
                    model.getVertices()[i + 5]
            );
            texCoords[j] = new Vector2D(
                    model.getVertices()[i + 6],
                    model.getVertices()[i + 7]
            );
            min.x = Math.min(min.x, vertices[j].x);
            min.y = Math.min(min.y, vertices[j].y);
            min.z = Math.min(min.z, vertices[j].z);
            max.x = Math.max(max.x, vertices[j].x);
            max.y = Math.max(max.y, vertices[j].y);
            max.z = Math.max(max.z, vertices[j].z);
            i+=8;
        }

        i = 0;
        for(int t = 0; t < triangles.length; t++) {
            triangles[t] = new Triangle(
                    vertices[i],
                    vertices[i+1],
                    vertices[i+2],
                    normals[i],
                    normals[i+1],
                    normals[i+2],
                    texCoords[i],
                    texCoords[i+1],
                    texCoords[i+2]
            );
            i+=3;
        }

        Mesh mesh = new Mesh(model.getPath(), triangles, min, max, smooth);
        return new Model(getTransform(), getMaterial().toRayDreamMaterial(), mesh);
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
