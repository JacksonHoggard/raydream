package me.jacksonhoggard.raydream.gui.editor.material;

import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.material.bxdf.BxDF;
import me.jacksonhoggard.raydream.material.bxdf.disney.DisneyDiffuse;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.util.Util;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;

public class EditorObjectMaterial<T extends BxDF> {

    private float[] albedo;
    private float[] emittance;
    private float indexOfRefraction;
    private Texture texture;
    private Texture bumpMap;
    private float bumpScale;
    private final HashMap<String, Object> parameters;
    private Class<T> bxdfClass;

    public static <T extends BxDF> EditorObjectMaterial<T> of(
            Class<T> bxdfClass,
            float[] color,
            float[] emittance,
            float indexOfRefraction,
            float bumpScale,
            HashMap<String, Object> parameters
    ) {
        return new EditorObjectMaterial<T>(
                bxdfClass,
                color,
                emittance,
                indexOfRefraction,
                bumpScale,
                parameters
        );
    }

    private EditorObjectMaterial(Class<T> bxdfClass,
        float[] color,
        float[] emittance,
        float indexOfRefraction,
        float bumpScale,
        HashMap<String, Object> parameters
    ) {
        this.albedo = color;
        this.emittance = emittance;
        this.indexOfRefraction = indexOfRefraction;
        this.bumpScale = bumpScale;
        this.parameters = new HashMap<>();
        this.bxdfClass = bxdfClass;
    }

    public EditorObjectMaterial(EditorObjectMaterial<T> material) {
        this.bxdfClass = material.bxdfClass;
        this.albedo = new float[] {
                material.albedo[0],
                material.albedo[1],
                material.albedo[2]
        };
        this.emittance = new float[] {
                material.emittance[0],
                material.emittance[1],
                material.emittance[2]
        };
        this.indexOfRefraction = material.indexOfRefraction;
        this.bumpScale = material.bumpScale;
        this.texture = material.texture;
        this.bumpMap = material.bumpMap;
        this.bumpScale = material.bumpScale;
        this.parameters = new HashMap<>(material.parameters);
    }

    public EditorObjectMaterial() {
        this(
            (Class<T>) DisneyDiffuse.class,
            new float[]{0.8f, 0.8f, 0.8f}, // Default gray
            new float[]{0.0f, 0.0f, 0.0f}, // Default black
            1.5f, // Default glass IOR
            1.0f,
            new HashMap<>() {{
                put("roughness", (Object) Double.valueOf(0.5D));
                put("subsurface", (Object) Double.valueOf(0.0D));
            }}
        );
    }

    public Material<? extends BxDF> toRayDreamMaterial() {
        return Material.of(
                bxdfClass,
                new Vector3D(albedo[0], albedo[1], albedo[2]),
                new Vector3D(emittance[0], emittance[1], emittance[2]),
                indexOfRefraction,
                texture != null ? Util.loadTexture(texture.getPath()) : null,
                bumpMap != null ? Util.loadBumpMap(bumpMap.getPath(), bumpScale) : null,
                parameters
        );
    }

    public void setBxDFClass(Class<T> bxdfClass) {
        this.parameters.clear();
        this.bxdfClass = bxdfClass;
        if(bxdfClass.equals(DisneyDiffuse.class)) {
            this.parameters.put("roughness", (Object) Double.valueOf(0.5D));
            this.parameters.put("subsurface", (Object) Double.valueOf(0.0D));
        }
    }

    public Class<T> getBxDFClass() {
        return bxdfClass;
    }

    public float[] getAlbedo() {
        return albedo;
    }

    public void setAlbedo(float[] color) {
        this.albedo = color;
    }

    public float getIndexOfRefraction() {
        return indexOfRefraction;
    }

    public void setIndexOfRefraction(float indexOfRefraction) {
        this.indexOfRefraction = indexOfRefraction;
    }

    public float[] getEmittance() {
        return emittance;
    }

    public void setEmittance(float[] emittance) {
        this.emittance = emittance;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public Texture getBumpMap() {
        return bumpMap;
    }

    public void setBumpMap(Texture bumpMap) {
        this.bumpMap = bumpMap;
    }

    public float getBumpScale() {
        return bumpScale;
    }

    public void setBumpScale(float bumpScale) {
        this.bumpScale = bumpScale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EditorObjectMaterial that)) return false;
        return Objects.deepEquals(albedo, that.albedo)
            && Objects.deepEquals(emittance, that.emittance)
            && Float.compare(indexOfRefraction, that.indexOfRefraction) == 0
            && Objects.equals(texture, that.texture)
            && Objects.equals(bumpMap, that.bumpMap)
            && Float.compare(bumpScale, that.bumpScale) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            Arrays.hashCode(albedo),
            Arrays.hashCode(emittance),
            indexOfRefraction,
            texture,
            bumpMap,
            bumpScale
        );
    }

    public String toSaveEntry(String path) {
        String texPath = "null";
        String bumpPath = "null";
        if(texture != null)
            texPath = Paths.get(path).relativize(Paths.get(texture.getPath())).toString();
        if(bumpMap != null)
            bumpPath = Paths.get(path).relativize(Paths.get(bumpMap.getPath())).toString();
        return "material:\n" +
                "| type: " + bxdfClass.getSimpleName() + "\n" +
                "| albedo: " + albedo[0] + " " + albedo[1] + " " + albedo[2] + "\n" +
                "| emittance: " + emittance[0] + " " + emittance[1] + " " + emittance[2] + "\n" +
                "| indexOfRefraction: " + indexOfRefraction + "\n" +
                "| texture: " + texPath + "\n" +
                "| bump: " + bumpPath + "\n" +
                "| bScale: " + bumpScale + "\n" +
                "| parameters:\n" +
                "| " + parameters.entrySet().stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .reduce((a, b) -> a + "\n| " + b).orElse("") + "\n/\n" +
                "/\n";
    }

    public HashMap<String, Object> getParameters() {
        return parameters;
    }

    public void setParameter(String key, Object value) {
        this.parameters.put(key, value);
    }

    public void showParameters(
        ImFloat inputFloat,
        ImBoolean inputBoolean
    ) {
        switch(bxdfClass.getSimpleName()) {
            case "DisneyDiffuse" -> showDisneyDiffuseParameters(inputFloat, inputBoolean);
            case "DisneyMetal" -> showDisneyMetalParameters(inputFloat, inputBoolean);
            case "DisneyClearcoat" -> showDisneyClearcoatParameters(inputFloat, inputBoolean);
            case "DisneySheen" -> showDisneySheenParameters(inputFloat, inputBoolean);
            case "DisneyGlass" -> showDisneyGlassParameters(inputFloat, inputBoolean);
        }
    }

    private void showDisneyDiffuseParameters(
        ImFloat inputFloat,
        ImBoolean inputBoolean
    ) {
        Object roughness = this.parameters.get("roughness");
        if(roughness == null) {
            this.parameters.put("roughness", (Object) Double.valueOf(0.5D));
        } else if(!(roughness instanceof Double)) {
            this.parameters.put("roughness", (Object) Double.valueOf(((Number) roughness).doubleValue()));
        }
        Object subsurface = this.parameters.get("subsurface");
        if(subsurface == null) {
            this.parameters.put("subsurface", (Object) Double.valueOf(0.0D));
        } else if(!(subsurface instanceof Double)) {
            this.parameters.put("subsurface", (Object) Double.valueOf(((Number) subsurface).doubleValue()));
        }
        inputFloat.set(((Double) this.parameters.get("roughness")).floatValue());
        ImGui.inputFloat("Roughness", inputFloat);
        this.parameters.put("roughness", (Object) Double.valueOf(inputFloat.get()));
        inputFloat.set(((Double) this.parameters.get("subsurface")).floatValue());
        ImGui.inputFloat("Subsurface", inputFloat);
        this.parameters.put("subsurface", (Object) Double.valueOf(inputFloat.get()));
    }

    private void showDisneyMetalParameters(
        ImFloat inputFloat,
        ImBoolean inputBoolean
    ) {
        Object roughness = this.parameters.get("roughness");
        if(roughness == null) {
            this.parameters.put("roughness", (Object) Double.valueOf(0.5D));
        } else if(!(roughness instanceof Double)) {
            this.parameters.put("roughness", (Object) Double.valueOf(((Number) roughness).doubleValue()));
        }
        Object anisotropic = this.parameters.get("anisotropic");
        if(anisotropic == null) {
            this.parameters.put("anisotropic", (Object) Double.valueOf(0.0D));
        } else if(!(anisotropic instanceof Double)) {
            this.parameters.put("anisotropic", (Object) Double.valueOf(((Number) anisotropic).doubleValue()));
        }
        inputFloat.set(((Double) this.parameters.get("roughness")).floatValue());
        ImGui.inputFloat("Roughness", inputFloat);
        this.parameters.put("roughness", (Object) Double.valueOf(inputFloat.get()));
        inputFloat.set(((Double) this.parameters.get("anisotropic")).floatValue());
        ImGui.inputFloat("Anisotropic", inputFloat);
        this.parameters.put("anisotropic", (Object) Double.valueOf(inputFloat.get()));
    }

    private void showDisneyClearcoatParameters(
        ImFloat inputFloat,
        ImBoolean inputBoolean
    ) {
        Object clearcoatGloss = this.parameters.get("clearcoatGloss");
        if(clearcoatGloss == null) {
            this.parameters.put("clearcoatGloss", (Object) Double.valueOf(0.0D));
        } else if(!(clearcoatGloss instanceof Double)) {
            this.parameters.put("clearcoatGloss", (Object) Double.valueOf(((Number) clearcoatGloss).doubleValue()));
        }
        inputFloat.set(((Double) this.parameters.get("clearcoatGloss")).floatValue());
        ImGui.inputFloat("Clearcoat Gloss", inputFloat);
        this.parameters.put("clearcoatGloss", (Object) Double.valueOf(inputFloat.get()));
    }

    private void showDisneySheenParameters(
        ImFloat inputFloat,
        ImBoolean inputBoolean
    ) {
    }

    private void showDisneyGlassParameters(
        ImFloat inputFloat,
        ImBoolean inputBoolean
    ) {
        Object roughness = this.parameters.get("roughness");
        if(roughness == null) {
            this.parameters.put("roughness", (Object) Double.valueOf(0.5D));
        } else if(!(roughness instanceof Double)) {
            this.parameters.put("roughness", (Object) Double.valueOf(((Number) roughness).doubleValue()));
        }
        Object anisotropic = this.parameters.get("anisotropic");
        if(anisotropic == null) {
            this.parameters.put("anisotropic", (Object) Double.valueOf(0.0D));
        } else if(!(anisotropic instanceof Double)) {
            this.parameters.put("anisotropic", (Object) Double.valueOf(((Number) anisotropic).doubleValue()));
        }
        Object ior = this.parameters.get("ior");
        if(ior == null) {
            this.parameters.put("ior", (Object) Double.valueOf(1.5D));
        } else if(!(ior instanceof Double)) {
            this.parameters.put("ior", (Object) Double.valueOf(((Number) ior).doubleValue()));
        }
        inputFloat.set(((Double) this.parameters.get("roughness")).floatValue());
        ImGui.inputFloat("Roughness", inputFloat);
        this.parameters.put("roughness", (Object) Double.valueOf(inputFloat.get()));
        inputFloat.set(((Double) this.parameters.get("anisotropic")).floatValue());
        ImGui.inputFloat("Anisotropic", inputFloat);
        this.parameters.put("anisotropic", (Object) Double.valueOf(inputFloat.get()));
        inputFloat.set(((Double) this.parameters.get("ior")).floatValue());
        ImGui.inputFloat("IOR", inputFloat);
        this.parameters.put("ior", (Object) Double.valueOf(inputFloat.get()));
    }
}
