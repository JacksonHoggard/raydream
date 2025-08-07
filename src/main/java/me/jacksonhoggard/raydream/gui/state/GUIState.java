package me.jacksonhoggard.raydream.gui.state;

import me.jacksonhoggard.raydream.gui.editor.light.EditorLight;
import me.jacksonhoggard.raydream.gui.editor.object.EditorObject;

public class GUIState {
    private EditorObject selectedObject;
    private EditorLight selectedLight;
    private int selectedTab = 0;
    private boolean isMiddleMousePressed = false;
    private float camDistance;
    private int currentGizmoOperation = 0;
    private int currentMode = 1; // Mode.LOCAL
    private boolean useSnap = false;
    private boolean isEditorWindowHovering = false;
    
    // FPS tracking
    private double fpsTimer = 0.0;
    private int fps = 0;
    
    public EditorObject getSelectedObject() {
        return selectedObject;
    }
    
    public void setSelectedObject(EditorObject selectedObject) {
        this.selectedObject = selectedObject;
    }
    
    public EditorLight getSelectedLight() {
        return selectedLight;
    }
    
    public void setSelectedLight(EditorLight selectedLight) {
        this.selectedLight = selectedLight;
    }
    
    public int getSelectedTab() {
        return selectedTab;
    }
    
    public void setSelectedTab(int selectedTab) {
        this.selectedTab = selectedTab;
    }
    
    public boolean isMiddleMousePressed() {
        return isMiddleMousePressed;
    }
    
    public void setMiddleMousePressed(boolean middleMousePressed) {
        isMiddleMousePressed = middleMousePressed;
    }
    
    public float getCamDistance() {
        return camDistance;
    }
    
    public void setCamDistance(float camDistance) {
        this.camDistance = camDistance;
    }
    
    public int getCurrentGizmoOperation() {
        return currentGizmoOperation;
    }
    
    public void setCurrentGizmoOperation(int currentGizmoOperation) {
        this.currentGizmoOperation = currentGizmoOperation;
    }
    
    public int getCurrentMode() {
        return currentMode;
    }
    
    public void setCurrentMode(int currentMode) {
        this.currentMode = currentMode;
    }
    
    public boolean isUseSnap() {
        return useSnap;
    }
    
    public void setUseSnap(boolean useSnap) {
        this.useSnap = useSnap;
    }
    
    public boolean isEditorWindowHovering() {
        return isEditorWindowHovering;
    }
    
    public void setEditorWindowHovering(boolean editorWindowHovering) {
        isEditorWindowHovering = editorWindowHovering;
    }
    
    public double getFpsTimer() {
        return fpsTimer;
    }
    
    public void setFpsTimer(double fpsTimer) {
        this.fpsTimer = fpsTimer;
    }
    
    public int getFps() {
        return fps;
    }
    
    public void setFps(int fps) {
        this.fps = fps;
    }
    
    public void incrementFps() {
        this.fps++;
    }
    
    public void resetFps() {
        this.fps = 0;
    }
}
