package me.jacksonhoggard.raydream.gui.state;

import me.jacksonhoggard.raydream.gui.editor.light.EditorLight;
import me.jacksonhoggard.raydream.gui.editor.object.EditorObject;

public interface StateChangeListener {
    default void onSelectedObjectChanged(EditorObject object) {}
    default void onSelectedLightChanged(EditorLight light) {}
    default void onSelectedTabChanged(int tab) {}
    default void onGizmoOperationChanged(int operation) {}
    default void onSnapModeChanged(boolean useSnap) {}
}
