package me.jacksonhoggard.raydream.gui.editor.material;

import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.util.Util;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;

public class EditorObjectMaterial {

    // Disney BRDF
    private float[] albedo;
    private float subsurface;
    private float metallic;
    private float[] specular;
    private float specularTint;
    private float specularTransmission;
    private float roughness;
    private float anisotropic;
    private float sheen;
    private float sheenTint;
    private float clearcoat;
    private float clearcoatGloss;

    // Other parameters
    private float indexOfRefraction;
    private Material.Type type;
    private Texture texture;
    private Texture bumpMap;
    private float bumpScale;

    public EditorObjectMaterial(
        float[] color,
        float subsurface,
        float metallic,
        float[] specular,
        float specularTint,
        float specularTransmission,
        float roughness,
        float anisotropic,
        float sheen,
        float sheenTint,
        float clearcoat,
        float clearcoatGloss,
        float indexOfRefraction,
        Material.Type type,
        float bumpScale
    ) {
        this.albedo = color;
        this.subsurface = subsurface;
        this.metallic = metallic;
        this.specular = specular;
        this.specularTint = specularTint;
        this.specularTransmission = specularTransmission;
        this.roughness = roughness;
        this.anisotropic = anisotropic;
        this.sheen = sheen;
        this.sheenTint = sheenTint;
        this.clearcoat = clearcoat;
        this.clearcoatGloss = clearcoatGloss;
        this.indexOfRefraction = indexOfRefraction;
        this.type = type;
        this.bumpScale = bumpScale;
    }

    public EditorObjectMaterial(EditorObjectMaterial material) {
        this.albedo = new float[] {
                material.albedo[0],
                material.albedo[1],
                material.albedo[2]
        };
        this.subsurface = material.subsurface;
        this.metallic = material.metallic;
        this.specular = material.specular;
        this.specularTint = material.specularTint;
        this.specularTransmission = material.specularTransmission;
        this.roughness = material.roughness;
        this.anisotropic = material.anisotropic;
        this.sheen = material.sheen;
        this.sheenTint = material.sheenTint;
        this.clearcoat = material.clearcoat;
        this.clearcoatGloss = material.clearcoatGloss;
        this.indexOfRefraction = material.indexOfRefraction;
        this.type = material.type;
        this.bumpScale = material.bumpScale;
    }

    public EditorObjectMaterial() {
        this.albedo = new float[]{0.8f, 0.8f, 0.8f}; // Default gray
        this.subsurface = 0.0f;
        this.metallic = 0.0f;
        this.specular = new float[]{0.5f, 0.5f, 0.5f}; // Default specular
        this.specularTint = 0.0f;
        this.specularTransmission = 0.0f;
        this.roughness = 0.5f; // Default medium roughness
        this.anisotropic = 0.0f;
        this.sheen = 0.0f;
        this.sheenTint = 0.5f;
        this.clearcoat = 0.0f;
        this.clearcoatGloss = 1.0f;
        this.indexOfRefraction = 1.5f; // Default glass IOR
        this.type = Material.Type.REFLECT;
        this.bumpScale = 1.0f;
    }

    public Material toRayDreamMaterial() {
        return new Material(
                new Vector3D(albedo[0], albedo[1], albedo[2]),
                subsurface,
                metallic,
                new Vector3D(specular[0], specular[1], specular[2]),
                specularTint,
                specularTransmission,
                roughness,
                anisotropic,
                sheen,
                sheenTint,
                clearcoat,
                clearcoatGloss,
                indexOfRefraction,
                type,
                texture != null ? Util.loadTexture(texture.getPath()) : null,
                bumpMap != null ? Util.loadBumpMap(bumpMap.getPath(), bumpScale) : null
        );
    }

    public float[] getAlbedo() {
        return albedo;
    }

    public void setAlbedo(float[] color) {
        this.albedo = color;
    }

    public void setSubsurface(float subsurface) {
        this.subsurface = subsurface;
    }

    public void setMetallic(float metallic) {
        this.metallic = metallic;
    }

    public float getSubsurface() {
        return subsurface;
    }

    public float getMetallic() {
        return metallic;
    }

    public void setSpecular(float[] specular) {
        this.specular = specular;
    }

    public float[] getSpecular() {
        return specular;
    }

    public float getSpecularTint() {
        return specularTint;
    }

    public void setSpecularTint(float specularTint) {
        this.specularTint = specularTint;
    }

    public float getSpecularTransmission() {
        return specularTransmission;
    }

    public void setSpecularTransmission(float specularTransmission) {
        this.specularTransmission = specularTransmission;
    }

    public float getRoughness() {
        return roughness;
    }

    public void setRoughness(float roughness) {
        this.roughness = roughness;
    }

    public float getAnisotropic() {
        return anisotropic;
    }

    public void setAnisotropic(float anisotropic) {
        this.anisotropic = anisotropic;
    }

    public float getSheen() {
        return sheen;
    }

    public void setSheen(float sheen) {
        this.sheen = sheen;
    }

    public float getSheenTint() {
        return sheenTint;
    }

    public void setSheenTint(float sheenTint) {
        this.sheenTint = sheenTint;
    }

    public float getClearcoat() {
        return clearcoat;
    }

    public void setClearcoat(float clearcoat) {
        this.clearcoat = clearcoat;
    }

    public float getClearcoatGloss() {
        return clearcoatGloss;
    }

    public void setClearcoatGloss(float clearcoatGloss) {
        this.clearcoatGloss = clearcoatGloss;
    }

    public float getIndexOfRefraction() {
        return indexOfRefraction;
    }

    public void setIndexOfRefraction(float indexOfRefraction) {
        this.indexOfRefraction = indexOfRefraction;
    }

    public void setType(Material.Type type) {
        this.type = type;
    }

    public Material.Type getType() {
        return type;
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
        return Float.compare(subsurface, that.subsurface) == 0
        && Float.compare(metallic, that.metallic) == 0
        && Objects.deepEquals(specular, that.specular)
        && Float.compare(indexOfRefraction, that.indexOfRefraction) == 0
        && Float.compare(specularTint, that.specularTint) == 0
        && Float.compare(specularTransmission, that.specularTransmission) == 0
        && Float.compare(bumpScale, that.bumpScale) == 0
        && Float.compare(anisotropic, that.anisotropic) == 0
        && Float.compare(sheen, that.sheen) == 0
        && Float.compare(sheenTint, that.sheenTint) == 0
        && Float.compare(clearcoat, that.clearcoat) == 0
        && Float.compare(clearcoatGloss, that.clearcoatGloss) == 0
        && Float.compare(roughness, that.roughness) == 0
        && Objects.deepEquals(albedo, that.albedo)
        && type == that.type
        && Objects.equals(texture, that.texture)
        && Objects.equals(bumpMap, that.bumpMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            Arrays.hashCode(albedo),
            subsurface,
            metallic,
            specular,
            specularTint,
            roughness,
            anisotropic,
            sheen,
            sheenTint,
            clearcoat,
            clearcoatGloss,
            indexOfRefraction,
            type,
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
                "| albedo: " + albedo[0] + " " + albedo[1] + " " + albedo[2] + "\n" +
                "| subsurface: " + subsurface + "\n" +
                "| metallic: " + metallic + "\n" +
                "| specular: " + specular[0] + " " + specular[1] + " " + specular[2] + "\n" +
                "| specularTint: " + specularTint + "\n" +
                "| specularTransmission: " + specularTransmission + "\n" +
                "| roughness: " + roughness + "\n" +
                "| anisotropic: " + anisotropic + "\n" +
                "| sheen: " + sheen + "\n" +
                "| sheenTint: " + sheenTint + "\n" +
                "| clearcoat: " + clearcoat + "\n" +
                "| clearcoatGloss: " + clearcoatGloss + "\n" +
                "| indexOfRefraction: " + indexOfRefraction + "\n" +
                "| type: " + type + "\n" +
                "| texture: " + texPath + "\n" +
                "| bump: " + bumpPath + "\n" +
                "| bScale: " + bumpScale + "\n" +
                "/\n";
    }
}
