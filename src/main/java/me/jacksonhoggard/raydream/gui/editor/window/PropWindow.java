package me.jacksonhoggard.raydream.gui.editor.window;

import imgui.ImGui;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import me.jacksonhoggard.raydream.gui.MenuBar;
import me.jacksonhoggard.raydream.gui.editor.light.EditorAreaLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorPointLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorSphereLight;
import me.jacksonhoggard.raydream.gui.editor.material.EditorLightMaterial;
import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.material.Texture;
import me.jacksonhoggard.raydream.gui.editor.object.EditorObject;
import me.jacksonhoggard.raydream.material.Material;

import java.io.IOException;
import java.util.Arrays;

public class PropWindow {

    private static float posX;
    private static float posY;
    private static float width;
    private static float height;

    private static final float[] translationMatrix = new float[3];
    private static final float[] rotationMatrix = new float[3];
    private static final float[] scaleMatrix = new float[3];

    private static final ImFloat inputFloat = new ImFloat();
    private static final float[] inputSnapValue = new float[]{1f, 1f, 1f};

    private static EditorObject selectedObject;
    private static EditorLight selectedLight;
    public static final int TRANSFORM_TAB = 0;
    public static final int MATERIAL_TAB = 1;
    private static int selectedTab = 0;

    private static final ImInt selectedMaterialType = new ImInt();
    private static final String[] MATERIAL_TYPES = new String[] {"Reflect", "Reflect & Refract", "Non-reflective"};

    public static void show() {
        width = ImGui.getMainViewport().getSizeX() / 5;
        height = ImGui.getMainViewport().getSizeY() - EditorWindow.getPosY();
        posX = 0;
        posY = MenuBar.getHeight();
        ImGui.setNextWindowPos(posX, posY, ImGuiCond.Always);
        ImGui.setNextWindowSize(width, height, ImGuiCond.Always);
        if (ImGui.begin("Properties", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoBringToFrontOnFocus)) {
            selectedObject = ObjectWindow.getSelectedObject();
            selectedLight = ObjectWindow.getSelectedLight();
            if(selectedObject != null || selectedLight != null) {
                if(ImGui.button("Transform"))
                    selectedTab = TRANSFORM_TAB;
                ImGui.sameLine();
                if(ImGui.button("Material"))
                    selectedTab = MATERIAL_TAB;

                ImGui.separator();

                switch(selectedTab) {
                    case TRANSFORM_TAB:
                        showTransformTab();
                        break;
                    case MATERIAL_TAB:
                        showMaterialTab();
                        break;
                }
            }
        }

        ImGui.end();
    }

    private static void showTransformTab() {
        if(selectedObject != null) {
            ImGuizmo.decomposeMatrixToComponents(selectedObject.getModelMatrix(), translationMatrix, rotationMatrix, scaleMatrix);
            ImGui.inputFloat3("Tr", translationMatrix, "%.3f");
            ImGui.inputFloat3("Rt", rotationMatrix, "%.3f");
            ImGui.inputFloat3("Sc", scaleMatrix, "%.3f");
            ImGuizmo.recomposeMatrixFromComponents(selectedObject.getModelMatrix(), translationMatrix, rotationMatrix, scaleMatrix);

            if (EditorWindow.getCurrentGizmoOperation() != Operation.SCALE) {
                if (ImGui.radioButton("Local", EditorWindow.getCurrentMode() == Mode.LOCAL)) {
                    EditorWindow.setCurrentMode(Mode.LOCAL);
                }
                ImGui.sameLine();
                if (ImGui.radioButton("World", EditorWindow.getCurrentMode() == Mode.WORLD)) {
                    EditorWindow.setCurrentMode(Mode.WORLD);
                }
            }

            ImGui.checkbox("Snap", EditorWindow.getUseSnap());
            inputFloat.set(inputSnapValue[0]);
            switch (EditorWindow.getCurrentGizmoOperation()) {
                case Operation.TRANSLATE:
                    ImGui.inputFloat3("Snap Value", inputSnapValue);
                    break;
                case Operation.ROTATE:
                    ImGui.inputFloat("Angle Value", inputFloat);
                    float rotateValue = inputFloat.get();
                    Arrays.fill(inputSnapValue, rotateValue);
                    break;
                case Operation.SCALE:
                    ImGui.inputFloat("Scale Value", inputFloat);
                    float scaleValue = inputFloat.get();
                    Arrays.fill(inputSnapValue, scaleValue);
                    break;
            }
        }
        if(selectedLight != null) {
            ImGuizmo.decomposeMatrixToComponents(selectedLight.getModelMatrix(), translationMatrix, rotationMatrix, scaleMatrix);
            if(selectedLight instanceof EditorSphereLight) {
                ImGui.inputFloat3("Position", translationMatrix, "%.3f");
                inputFloat.set(((EditorSphereLight) selectedLight).getRadius());
                ImGui.inputFloat("Radius", inputFloat);
                ((EditorSphereLight) selectedLight).setRadius(inputFloat.get());
                ImGuizmo.recomposeMatrixFromComponents(selectedLight.getModelMatrix(), translationMatrix, rotationMatrix, new float[]{inputFloat.get(), inputFloat.get(), inputFloat.get()});
            }
            if(selectedLight instanceof EditorPointLight) {
                ImGui.inputFloat3("Position", translationMatrix, "%.3f");
                ImGuizmo.recomposeMatrixFromComponents(selectedLight.getModelMatrix(), translationMatrix, rotationMatrix, scaleMatrix);
            }
            if(selectedLight instanceof EditorAreaLight) {
                ImGui.inputFloat3("Tr", translationMatrix, "%.3f");
                ImGui.inputFloat3("Rt", rotationMatrix, "%.3f");
                ImGui.inputFloat3("Sc", scaleMatrix, "%.3f");
                ImGuizmo.recomposeMatrixFromComponents(selectedLight.getModelMatrix(), translationMatrix, rotationMatrix, scaleMatrix);
            }
        }

    }

    private static void showMaterialTab() {
        if(selectedObject != null && selectedObject.getMaterial() != null) {
            EditorObjectMaterial material = selectedObject.getMaterial();
            if(material.getTexture() == null) {
                ImGui.inputFloat3("Color", material.getColor());
            }
            inputFloat.set(material.getAmbient());
            ImGui.inputFloat("Ambient", inputFloat);
            material.setAmbient(inputFloat.get());
            inputFloat.set(material.getDiffuse());
            ImGui.inputFloat("Diffuse", inputFloat);
            material.setDiffuse(inputFloat.get());
            inputFloat.set(material.getSpecular());
            ImGui.inputFloat("Specular", inputFloat);
            material.setSpecular(inputFloat.get());
            inputFloat.set(material.getSpecularExponent());
            ImGui.inputFloat("Specular Exponent", inputFloat);
            material.setSpecularExponent(inputFloat.get());
            if(material.getType().ordinal() != 2) {
                inputFloat.set(material.getIndexOfRefraction());
                ImGui.inputFloat("Index of Refraction", inputFloat);
                material.setIndexOfRefraction(inputFloat.get());
            }
            if(material.getType().ordinal() == 0) {
                inputFloat.set(material.getK());
                ImGui.inputFloat("K", inputFloat);
                material.setK(inputFloat.get());
            }
            inputFloat.set(material.getMetalness());
            ImGui.inputFloat("Metalness", inputFloat);
            material.setMetalness(inputFloat.get());
            if(material.getBumpMap() != null) {
                inputFloat.set(material.getBumpScale());
                ImGui.inputFloat("Bump Scale", inputFloat);
                material.setBumpScale(inputFloat.get());
            }

            selectedMaterialType.set(material.getType().ordinal());
            if (ImGui.combo("Type", selectedMaterialType, MATERIAL_TYPES)) {
                switch (selectedMaterialType.get()) {
                    case 0:
                        material.setType(Material.Type.REFLECT);
                        break;
                    case 1:
                        material.setType(Material.Type.REFLECT_REFRACT);
                        break;
                    case 2:
                        material.setType(Material.Type.OTHER);
                        break;
                }
            }
            if(ImGui.button("Choose texture")) {
                String path = DialogWindow.openFileChooser("Image files", "png", "jpg", "bmp");
                if(path != null) {
                    if(material.getTexture() != null)
                        material.getTexture().remove();
                    try {
                        material.setTexture(new Texture(path));
                    } catch (IOException e) {
                        DialogWindow.showError("Unable to load texture: ", e);
                    }
                }
            }
            if(material.getTexture() != null) {
                ImGui.text(material.getTexture().getPath());
                if(ImGui.button("Remove texture")) {
                    material.getTexture().remove();
                    material.setTexture(null);
                }
            }
            if(ImGui.button("Choose bump map")) {
                String path = DialogWindow.openFileChooser("Image files", "png", "jpg", "bmp");
                if(path != null) {
                    if(material.getBumpMap() != null)
                        material.getBumpMap().remove();
                    try {
                        material.setBumpMap(new Texture(path));
                    } catch(IOException e) {
                        DialogWindow.showError("Unable to load bump map: ", e);
                    }
                }
            }
            if(material.getBumpMap() != null) {
                ImGui.text(material.getBumpMap().getPath());
                if(ImGui.button("Remove bump map")) {
                    material.getBumpMap().remove();
                    material.setBumpMap(null);
                }
            }
        }
        if(selectedLight != null) {
            EditorLightMaterial material = selectedLight.getMaterial();
            ImGui.inputFloat3("Color", material.getColor());
            inputFloat.set(material.getBrightness());
            ImGui.inputFloat("Brightness", inputFloat);
            material.setBrightness(inputFloat.get());
        }
    }

    public static float[] getInputSnapValue() {
        return inputSnapValue;
    }

    public static int getSelectedTab() {
        return selectedTab;
    }

    public static float getPosX() {
        return posX;
    }

    public static float getPosY() {
        return posY;
    }

    public static float getWidth() {
        return width;
    }

    public static float getHeight() {
        return height;
    }

}
