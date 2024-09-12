package me.jacksonhoggard.raydream.gui.editor.material;

public class EditorLightMaterial {

    private float[] color;
    private float brightness;

    public EditorLightMaterial(float[] color, float brightness) {
        this.color = color;
        this.brightness = brightness;
    }

    public EditorLightMaterial() {
        color = new float[]{1.f, 1.f, 1.f};
        brightness = 1.f;
    }

    public void setColor(float[] color) {
        this.color = color;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public float[] getColor() {
        return color;
    }

    public float getBrightness() {
        return brightness;
    }

}
