package me.jacksonhoggard.raydream.gui.editor.object;

import imgui.extension.imguizmo.ImGuizmo;
import me.jacksonhoggard.raydream.gui.editor.material.EditorMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.EditorModel;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Transform;

public abstract class EditorObject implements IEditorObject {

    protected static int selected = -1;
    private static int lastID = 0;
    protected final int id;
    protected final String label;
    private EditorMaterial material;
    private final float[] modelMatrix = {
            1.f, 0.f, 0.f, 0.f,
            0.f, 1.f, 0.f, 0.f,
            0.f, 0.f, 1.f, 0.f,
            0.f, 0.f, 0.f, 1.f
    };

    private final EditorModel model;

    public EditorObject(EditorModel model, EditorMaterial material) {
        this.material = material;
        this.model = model;
        this.model.create();
        id = lastID;
        label = "Object " + id;
        lastID++;
    }

    public EditorModel getModel() {
        return model;
    }

    public float[] getModelMatrix() {
        return modelMatrix;
    }

    public Transform getTransform() {
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        ImGuizmo.decomposeMatrixToComponents(getModelMatrix(), translation, rotation, scale);
        return new Transform(
                new Vector3D(translation[0], translation[1], translation[2]),
                new Vector3D(rotation[0], rotation[1], rotation[2]),
                new Vector3D(scale[0], scale[1], scale[2])
        );
    }

    public void setMaterial(EditorMaterial material) {
        this.material = material;
    }

    public EditorMaterial getMaterial() {
        return material;
    }

    public int getId() {
        return id;
    }

    public static void setSelected(int selected) {
        EditorObject.selected = selected;
    }

    public static int getSelected() {
        return selected;
    }
}
