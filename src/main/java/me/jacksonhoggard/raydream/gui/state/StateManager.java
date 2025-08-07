package me.jacksonhoggard.raydream.gui.state;

import me.jacksonhoggard.raydream.gui.editor.light.EditorLight;
import me.jacksonhoggard.raydream.gui.editor.object.EditorObject;

import java.util.ArrayList;
import java.util.List;

public class StateManager {
    private final GUIState state = new GUIState();
    private final List<StateChangeListener> listeners = new ArrayList<>();
    
    public void addListener(StateChangeListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(StateChangeListener listener) {
        listeners.remove(listener);
    }
    
    public GUIState getState() {
        return state;
    }
    
    public void updateSelectedObject(EditorObject object) {
        state.setSelectedObject(object);
        notifySelectedObjectChanged(object);
    }
    
    public void updateSelectedLight(EditorLight light) {
        state.setSelectedLight(light);
        notifySelectedLightChanged(light);
    }
    
    public void updateSelectedTab(int tab) {
        state.setSelectedTab(tab);
        notifySelectedTabChanged(tab);
    }
    
    public void updateGizmoOperation(int operation) {
        state.setCurrentGizmoOperation(operation);
        notifyGizmoOperationChanged(operation);
    }
    
    public void updateSnapMode(boolean useSnap) {
        state.setUseSnap(useSnap);
        notifySnapModeChanged(useSnap);
    }
    
    private void notifySelectedObjectChanged(EditorObject object) {
        for (StateChangeListener listener : listeners) {
            listener.onSelectedObjectChanged(object);
        }
    }
    
    private void notifySelectedLightChanged(EditorLight light) {
        for (StateChangeListener listener : listeners) {
            listener.onSelectedLightChanged(light);
        }
    }
    
    private void notifySelectedTabChanged(int tab) {
        for (StateChangeListener listener : listeners) {
            listener.onSelectedTabChanged(tab);
        }
    }
    
    private void notifyGizmoOperationChanged(int operation) {
        for (StateChangeListener listener : listeners) {
            listener.onGizmoOperationChanged(operation);
        }
    }
    
    private void notifySnapModeChanged(boolean useSnap) {
        for (StateChangeListener listener : listeners) {
            listener.onSnapModeChanged(useSnap);
        }
    }
}
