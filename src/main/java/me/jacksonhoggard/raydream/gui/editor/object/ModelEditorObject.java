package me.jacksonhoggard.raydream.gui.editor.object;

import imgui.extension.imguizmo.ImGuizmo;
import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.MeshModel;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Mesh;
import me.jacksonhoggard.raydream.object.Model;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.object.Triangle;

import java.io.*;
import java.nio.file.Paths;

public class ModelEditorObject extends EditorObject {

    public ModelEditorObject(MeshModel model) throws IOException {
        super(model, new EditorObjectMaterial());
    }

    public ModelEditorObject(MeshModel model, float[] translation, float[] rotation, float[] scale, String label) throws IOException {
        super(model, new EditorObjectMaterial());
        ImGuizmo.recomposeMatrixFromComponents(this.getModelMatrix(), translation, rotation, scale);
        this.label.set(label);
    }

    @Override
    public void draw() {
    }

    @Override
    public EditorObjectMaterial getMaterial() {
        if(!getSubIds().isEmpty()) {
            for(Integer i : getSubIds()) {
                if(i.intValue() == selected) {
                    return ((MeshModel) getModel()).getMeshes().get(getSubIds().indexOf(i)).getMaterial();
                }
            }
        }
        return null;
    }

    @Override
    public boolean isSelected() {
        for(Integer i : getSubIds()) {
            if(i.intValue() == selected) {
                return true;
            }
        }
        return super.isSelected();
    }

    @Override
    public Object toObject() {
        MeshModel model = (MeshModel) getModel();
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

    @Override
    public String toSaveEntry(String path) {
        String modelPath;
        try {
            modelPath = saveModel(path);
        } catch (IOException e) {
            throw new RuntimeException("Could not save model: ", e);
        }
        StringBuilder materials = new StringBuilder();
        for(MeshModel.Mesh m : ((MeshModel) getModel()).getMeshes()) {
            materials.append(m.getMaterial().toSaveEntry(path));
        }
        return "+ object: model\n" +
                "label: " + label.get() + "\n" +
                getTransformSaveEntry() + materials.toString() +
                "file: " + modelPath + "\n" +
                ";\n";
    }

    private String saveModel(String path) throws IOException {
        MeshModel model = (MeshModel) getModel();
        String fileName = Paths.get(model.getPath()).getFileName().toString();
        fileName = fileName.endsWith(".obj") ? fileName.substring(0, fileName.length() - 3) + "rdo" : fileName ;
        String filePath = path + File.separator + fileName;

        FileWriter writer = new FileWriter(filePath);
        for(MeshModel.Mesh m : model.getMeshes()) {

            writer.write("+ mesh:\n");
            writer.write("label: " + m.getLabel() + "\n");
            writer.write("triangles: \n");

            Vector3D[] vertices = new Vector3D[m.getVertexCount()];
            Vector3D[] normals = new Vector3D[m.getVertexCount()];
            Vector2D[] texCoords = new Vector2D[m.getVertexCount()];
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
                i+=8;
            }
            i = 0;
            for(int t = 0; t < m.getVertexCount() / 3; t++) {
                writer.write("| " + vecToSaveEntry(vertices[i]) + " " + vecToSaveEntry(vertices[i+1]) + " " + vecToSaveEntry(vertices[i+2]) + " "
                                + vecToSaveEntry(normals[i]) + " " + vecToSaveEntry(normals[i+1]) + " " + vecToSaveEntry(normals[i+2]) + " " +
                                vecToSaveEntry(texCoords[i]) + " " + vecToSaveEntry(texCoords[i+1]) + " " + vecToSaveEntry(texCoords[i+2]) + "\n");
                i+=3;
            }
            writer.write("/\n");
            writer.write(";\n");
        }

        writer.close();

        return Paths.get(path).relativize(Paths.get(filePath)).toString();
    }

    private static String vecToSaveEntry(Vector3D v) {
        return v.x + " " + v.y + " " + v.z;
    }

    private static String vecToSaveEntry(Vector2D v) {
        return v.x + " " + v.y;
    }

    public Model[] toObjects() {
        Model[] models = new Model[getSubIds().size()];

        int mIndex = 0;
        MeshModel model = (MeshModel) getModel();
        for(MeshModel.Mesh m : model.getMeshes()) {

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
        getModel().remove();
        if(getMaterial() != null && getMaterial().getTexture() != null)
            getMaterial().getTexture().remove();
        if(getMaterial() != null && getMaterial().getBumpMap() != null)
            getMaterial().getBumpMap().remove();
    }
}
