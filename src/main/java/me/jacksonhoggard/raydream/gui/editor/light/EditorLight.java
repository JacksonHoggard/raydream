package me.jacksonhoggard.raydream.gui.editor.light;

import imgui.ImGui;
import imgui.extension.imguizmo.ImGuizmo;
import me.jacksonhoggard.raydream.gui.editor.material.EditorLightMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.EditorModel;
import me.jacksonhoggard.raydream.gui.editor.object.EditorObject;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Transform;

public abstract class EditorLight implements IEditorLight {

    protected static int selected = -1;
    private static int lastID = 0;
    protected final int id;
    protected final String label;
    private EditorLightMaterial material;
    private final float[] modelMatrix = {
            1.f, 0.f, 0.f, 0.f,
            0.f, 1.f, 0.f, 0.f,
            0.f, 0.f, 1.f, 0.f,
            0.f, 0.f, 0.f, 1.f
    };

    private final EditorModel model;

    public EditorLight(EditorModel model, EditorLightMaterial material) {
        this.model = model;
        this.model.create();
        this.material = material;
        id = lastID;
        label = "Light " + id;
        lastID++;
    }

    @Override
    public void show() {
        ImGui.pushID(id);

        if(ImGui.selectable(label, id == selected)) {
            selected = id;
            EditorObject.setSelected(-1);
        }

        ImGui.popID();
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

    public void setMaterial(EditorLightMaterial material) {
        this.material = material;
    }

    public EditorLightMaterial getMaterial() {
        return material;
    }

    public int getId() {
        return id;
    }

    public static void setSelected(int selected) {
        EditorLight.selected = selected;
    }

    public static int getSelected() {
        return selected;
    }
}
