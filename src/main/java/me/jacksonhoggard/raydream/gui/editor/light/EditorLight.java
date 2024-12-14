package me.jacksonhoggard.raydream.gui.editor.light;

import imgui.ImGui;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiSelectableFlags;
import imgui.type.ImString;
import me.jacksonhoggard.raydream.gui.editor.material.EditorLightMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.EditorModel;
import me.jacksonhoggard.raydream.gui.editor.object.EditorObject;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Transform;

import java.io.IOException;

public abstract class EditorLight implements IEditorLight {

    protected static int selected = -1;
    private static int lastID = 0;
    protected final int id;
    protected ImString label;
    private boolean wasDoubleClicked = false;
    private boolean isEditingLabel = false;
    private EditorLightMaterial material;
    private final float[] modelMatrix = {
            1.f, 0.f, 0.f, 0.f,
            0.f, 1.f, 0.f, 0.f,
            0.f, 0.f, 1.f, 0.f,
            0.f, 0.f, 0.f, 1.f
    };

    private final EditorModel model;

    public EditorLight(EditorModel model, EditorLightMaterial material) throws IOException {
        this.model = model;
        this.model.create();
        this.material = material;
        id = lastID;
        label = new ImString("Light", 128);
        lastID++;
    }

    @Override
    public void show() {
        ImGui.pushID(id);

        if(!isEditingLabel) {
            if (ImGui.selectable(label.get(), id == selected, ImGuiSelectableFlags.AllowDoubleClick)) {
                selected = id;
                EditorObject.setSelected(-1);
                if (ImGui.isMouseDoubleClicked(0)) {
                    wasDoubleClicked = true;
                }
            }
        }

        if(wasDoubleClicked) {
            ImGui.sameLine();
            isEditingLabel = true;
            wasDoubleClicked = false;
        }

        if(isEditingLabel) {
            ImGui.inputText("##edit", label, ImGuiInputTextFlags.EnterReturnsTrue);
            if(ImGui.isItemDeactivated()) {
                isEditingLabel = false;
            }
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

    public boolean isSelected() {
        return selected == id;
    }

    public static void reset() {
        selected = -1;
        lastID = 0;
    }

    protected String getTransformSaveEntry() {
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        ImGuizmo.decomposeMatrixToComponents(getModelMatrix(), translation, rotation, scale);
        return "transform:\n" +
                "| translation: " + translation[0] + " " + translation[1] + " " + translation[2] + "\n"
                + "| rotation: " + rotation[0] + " " + rotation[1] + " " + rotation[2] + "\n"
                + "| scale: " + scale[0] + " " + scale[1] + " " + scale[2] + "\n" +
                "/\n";
    }
}
