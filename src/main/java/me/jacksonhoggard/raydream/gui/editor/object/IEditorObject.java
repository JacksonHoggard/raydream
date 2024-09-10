package me.jacksonhoggard.raydream.gui.editor.object;

import me.jacksonhoggard.raydream.object.Object;

public interface IEditorObject {

    void show();

    void draw();
    
    Object toObject();

}
