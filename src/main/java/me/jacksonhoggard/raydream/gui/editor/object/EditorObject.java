package me.jacksonhoggard.raydream.gui.editor.object;

import imgui.ImGui;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiSelectableFlags;
import imgui.type.ImString;
import me.jacksonhoggard.raydream.gui.editor.light.EditorLight;
import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.EditorModel;
import me.jacksonhoggard.raydream.gui.editor.model.MeshModel;
import me.jacksonhoggard.raydream.material.bxdf.BxDF;
import me.jacksonhoggard.raydream.math.Matrix4D;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Primitive;
import me.jacksonhoggard.raydream.object.Transform;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.opengl.GL;

public abstract class EditorObject implements IEditorObject {

    protected static int selected = -1;
    private static int lastID = 0;
    protected final int id;
    private final ArrayList<Integer> subIds;
    protected ImString label;
    private boolean wasDoubleClicked = false;
    private boolean isEditingLabel = false;
    private boolean isExpanded = false;
    private EditorObjectMaterial<? extends BxDF> material;
    private final float[] modelMatrix = {
            1.f, 0.f, 0.f, 0.f,
            0.f, 1.f, 0.f, 0.f,
            0.f, 0.f, 1.f, 0.f,
            0.f, 0.f, 0.f, 1.f
    };

    private final EditorModel model;

    public EditorObject(EditorModel model, EditorObjectMaterial<? extends BxDF> material) throws IOException {
        this.material = material;
        this.model = model;
        try {
            if(GL.getCapabilities() != null) {
                this.model.create();
            }
        } catch (Exception e) {
            // Do nothing
        }
        id = lastID;
        label = new ImString("Object", 128);
        lastID++;
        subIds = new ArrayList<>();
        if(model instanceof MeshModel) {
            for(MeshModel.Mesh _ : ((MeshModel) model).getMeshes()) {
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
            for(MeshModel.Mesh mesh : ((MeshModel) model).getMeshes()) {
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
        Vector3D translation = new Vector3D();
        Vector3D rotation = new Vector3D();
        Vector3D scale = new Vector3D();
        float[] mat = getModelMatrix();
        Matrix4D m = new Matrix4D(
            mat[0], mat[1], mat[2], mat[3],
            mat[4], mat[5], mat[6], mat[7],
            mat[8], mat[9], mat[10], mat[11],
            mat[12], mat[13], mat[14], mat[15]
        );
        Primitive.decomposeMatrix(m, translation, rotation, scale);
        return new Transform(
                translation,
                rotation,
                scale
        );
    }

    public void setMaterial(EditorObjectMaterial<? extends BxDF> material) {
        this.material = material;
    }

    public EditorObjectMaterial<? extends BxDF> getMaterial() {
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
                "| translation: " + translation[0] + " " + translation[1] + " " + translation[2] + "\n"
                + "| rotation: " + rotation[0] + " " + rotation[1] + " " + rotation[2] + "\n"
                + "| scale: " + scale[0] + " " + scale[1] + " " + scale[2] + "\n" +
                "/\n";
    }
}
