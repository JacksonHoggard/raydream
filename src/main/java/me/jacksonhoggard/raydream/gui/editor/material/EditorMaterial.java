package me.jacksonhoggard.raydream.gui.editor.material;

import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Vector3D;

public class EditorMaterial {

    private float[] color;
    private float ambient;
    private float diffuse;
    private float specular;
    private float specularExponent;
    private float indexOfRefraction;
    private float k;
    private float metalness;
    private Material.Type type;

    public EditorMaterial(float[] color, float ambient, float diffuse, float specular, float specularExponent, float indexOfRefraction, float k, float metalness, Material.Type type) {
        this.color = color;
        this.ambient = ambient;
        this.diffuse = diffuse;
        this.specular = specular;
        this.specularExponent = specularExponent;
        this.indexOfRefraction = indexOfRefraction;
        this.k = k;
        this.metalness = metalness;
        this.type = type;
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
                true
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
}
