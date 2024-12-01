package me.jacksonhoggard.raydream.gui.editor.material;

import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.util.Util;

import java.util.Arrays;
import java.util.Objects;

public class EditorObjectMaterial {

    private float[] color;
    private float ambient;
    private float diffuse;
    private float specular;
    private float specularExponent;
    private float indexOfRefraction;
    private float k;
    private float metalness;
    private Material.Type type;
    private Texture texture;
    private Texture bumpMap;
    private float bumpScale;

    public EditorObjectMaterial(float[] color, float ambient, float diffuse, float specular, float specularExponent, float indexOfRefraction, float k, float metalness, Material.Type type, float bumpScale) {
        this.color = color;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.specularExponent = specularExponent;
        this.indexOfRefraction = indexOfRefraction;
        this.k = k;
        this.metalness = metalness;
        this.type = type;
        this.bumpScale = bumpScale;
    }

    public EditorObjectMaterial(EditorObjectMaterial material) {
        this.color = new float[] {
                material.color[0],
                material.color[1],
                material.color[2]
        };
        this.ambient = material.ambient;
        this.diffuse = material.diffuse;
        this.specular = material.specular;
        this.specularExponent = material.specularExponent;
        this.indexOfRefraction = material.indexOfRefraction;
        this.k = material.k;
        this.metalness = material.metalness;
        this.type = material.type;
        this.texture = material.texture;
        this.bumpMap = material.bumpMap;
        this.bumpScale = material.bumpScale;
    }

    public Material toRayDreamMaterial() {
        return new Material(
                new Vector3D(color[0], color[1], color[2]),
                ambient,
                diffuse,
                specular,
                specularExponent,
                metalness,
                indexOfRefraction,
                k,
                type,
                texture != null ? Util.loadTexture(texture.getPath()) : null,
                bumpMap != null ? Util.loadBumpMap(bumpMap.getPath(), bumpScale) : null
        );
    }

    public float getAmbient() {
        return ambient;
    }

    public void setAmbient(float ambient) {
        this.ambient = ambient;
    }

    public float[] getColor() {
        return color;
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    public float getDiffuse() {
        return diffuse;
    }

    public void setDiffuse(float diffuse) {
        this.diffuse = diffuse;
    }

    public float getSpecular() {
        return specular;
    }

    public void setSpecular(float specular) {
        this.specular = specular;
    }

    public float getMetalness() {
        return metalness;
    }

    public void setMetalness(float metalness) {
        this.metalness = metalness;
    }

    public float getSpecularExponent() {
        return specularExponent;
    }

    public void setSpecularExponent(float specularExponent) {
        this.specularExponent = specularExponent;
    }

    public float getIndexOfRefraction() {
        return indexOfRefraction;
    }

    public void setIndexOfRefraction(float indexOfRefraction) {
        this.indexOfRefraction = indexOfRefraction;
    }

    public float getK() {
        return k;
    }

    public void setK(float k) {
        this.k = k;
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
        return Float.compare(ambient, that.ambient) == 0 && Float.compare(diffuse, that.diffuse) == 0 && Float.compare(specular, that.specular) == 0 && Float.compare(specularExponent, that.specularExponent) == 0 && Float.compare(indexOfRefraction, that.indexOfRefraction) == 0 && Float.compare(k, that.k) == 0 && Float.compare(metalness, that.metalness) == 0 && Objects.deepEquals(color, that.color) && type == that.type && Objects.equals(texture, that.texture);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(color), ambient, diffuse, specular, specularExponent, indexOfRefraction, k, metalness, type, texture);
    }
}
