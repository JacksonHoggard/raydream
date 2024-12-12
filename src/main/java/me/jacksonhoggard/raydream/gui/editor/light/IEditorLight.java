package me.jacksonhoggard.raydream.gui.editor.light;

import me.jacksonhoggard.raydream.light.Light;

public interface IEditorLight {

    void show();

    void draw();

    Light toLight();

    String toSaveEntry();

}
