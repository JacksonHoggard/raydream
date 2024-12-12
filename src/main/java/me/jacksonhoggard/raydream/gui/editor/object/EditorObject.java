package me.jacksonhoggard.raydream.gui.editor.object;

import imgui.ImGui;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiSelectableFlags;
import imgui.type.ImString;
import me.jacksonhoggard.raydream.gui.editor.light.EditorLight;
import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.EditorModel;
import me.jacksonhoggard.raydream.gui.editor.model.OBJModel;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Transform;

import java.util.ArrayList;

public abstract class EditorObject implements IEditorObject {

    protected static int selected = -1;
    private static int lastID = 0;
    protected final int id;
    private final ArrayList<Integer> subIds;
    protected ImString label;
    private boolean wasDoubleClicked = false;
    private boolean isEditingLabel = false;
    private boolean isExpanded = false;
    private EditorObjectMaterial material;
    private final float[] modelMatrix = {
            1.f, 0.f, 0.f, 0.f,
            0.f, 1.f, 0.f, 0.f,
            0.f, 0.f, 1.f, 0.f,
            0.f, 0.f, 0.f, 1.f
    };

    private final EditorModel model;

    public EditorObject(EditorModel model, EditorObjectMaterial material) {
        this.material = material;
        this.model = model;
        this.model.create();
        id = lastID;
        label = new ImString("Object", 128);
        lastID++;
        subIds = new ArrayList<>();
        if(model instanceof OBJModel) {
            for(OBJModel.Mesh _ : ((OBJModel) model).getMeshes()) {
                subIds.add(lastID);
                lastID++;
            }
        }
    }

    @Override
    public void show() {
        ImGui.pushID(id);

        if(!subIds.isEmpty()) {
            if(ImGui.button(isExpanded ? "-" : "+")) {
                isExpanded = !isExpanded;
            }
            ImGui.sameLine();
        }

        if(!isEditingLabel) {
            if (ImGui.selectable(label.get(), id == selected, ImGuiSelectableFlags.AllowDoubleClick)) {
                selected = id;
                EditorLight.setSelected(-1);
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

        if(isExpanded) {
            ImGui.indent();
            int i = 0;
            for(OBJModel.Mesh mesh : ((OBJModel) model).getMeshes()) {
                ImGui.pushID(subIds.get(i).intValue());

                if(ImGui.selectable(mesh.getLabel(), subIds.get(i).intValue() == selected)) {
                    selected = subIds.get(i).intValue();
                    EditorLight.setSelected(-1);
                }

                ImGui.popID();
                i++;
            }
            ImGui.unindent();
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

    public void setMaterial(EditorObjectMaterial material) {
        this.material = material;
    }

    public EditorObjectMaterial getMaterial() {
        return material;
    }

    public int getId() {
        return id;
    }

    public ArrayList<Integer> getSubIds() {
        return subIds;
    }

    public static void setSelected(int selected) {
        EditorObject.selected = selected;
    }

    public static int getSelected() {
        return selected;
    }

    public boolean isSelected() {
        return selected == id;
    }

    @Override
    public void remove() {
        getModel().remove();
        if(getMaterial().getTexture() != null)
            getMaterial().getTexture().remove();
        if(getMaterial().getBumpMap() != null)
            getMaterial().getBumpMap().remove();
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
                "| " + translation[0] + " " + translation[1] + " " + translation[2] + "\n"
                + "| " + rotation[0] + " " + rotation[1] + " " + rotation[2] + "\n"
                + "| " + scale[0] + " " + scale[1] + " " + scale[2] + "\n" +
                "/\n";
    }
}
