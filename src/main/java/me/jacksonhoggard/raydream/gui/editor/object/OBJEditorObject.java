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

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class OBJEditorObject extends EditorObject {

    public OBJEditorObject(String path, EditorObjectMaterial material) {
        super(new OBJModel(path), material);
    }

    public OBJEditorObject(String path) {
        super(new OBJModel(path), new EditorObjectMaterial(
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
    }

    @Override
    public void draw() {
    }

    @Override
    public EditorObjectMaterial getMaterial() {
        if(!getSubIds().isEmpty()) {
            for(Integer i : getSubIds()) {
                if(i.intValue() == selected) {
                    return ((OBJModel) getModel()).getMeshes().get(getSubIds().indexOf(i)).getMaterial();
                }
            }
        }
        return null;
    }

    @Override
    public Object toObject() {
        OBJModel model = (OBJModel) getModel();
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

        Mesh mesh = new Mesh(model.getPath(), triangles, min, max);
        return new Model(getTransform(), getMaterial().toRayDreamMaterial(), mesh);
    }

    public Model[] toObjects() {
        Model[] models = new Model[getSubIds().size()];

        int mIndex = 0;
        OBJModel model = (OBJModel) getModel();
        for(OBJModel.Mesh m : model.getMeshes()) {

            Vector3D[] vertices = new Vector3D[m.getVertexCount()];
            Vector3D[] normals = new Vector3D[m.getVertexCount()];
            Vector2D[] texCoords = new Vector2D[m.getVertexCount()];
            Triangle[] triangles = new Triangle[m.getVertexCount() / 3];
            Vector3D min = new Vector3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
            Vector3D max = new Vector3D(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

            int i = 0;
            for(int j = 0; j < vertices.length; j++) {
                vertices[j] = new Vector3D(
                        m.getVertices()[i],
                        m.getVertices()[i + 1],
                        m.getVertices()[i + 2]
                );
                normals[j] = new Vector3D(
                        m.getVertices()[i + 3],
                        m.getVertices()[i + 4],
                        m.getVertices()[i + 5]
                );
                texCoords[j] = new Vector2D(
                        m.getVertices()[i + 6],
                        m.getVertices()[i + 7]
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

            Mesh mesh = new Mesh(model.getPath(), triangles, min, max);
            models[mIndex] = new Model(getTransform(), m.getMaterial().toRayDreamMaterial(), mesh);
            mIndex++;
        }
        return models;
    }

    @Override
    public void remove() {
        super.remove();
    }
}
