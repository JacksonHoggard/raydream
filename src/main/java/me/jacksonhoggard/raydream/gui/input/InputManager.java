package me.jacksonhoggard.raydream.gui.input;

import me.jacksonhoggard.raydream.config.ApplicationConfig;
import me.jacksonhoggard.raydream.gui.state.StateManager;
import me.jacksonhoggard.raydream.gui.editor.window.EditorWindow;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

public class InputManager {
    private final Map<Integer, Runnable> keyBindings = new HashMap<>();
    private final StateManager stateManager;
    
    public InputManager(StateManager stateManager) {
        this.stateManager = stateManager;
        initializeDefaultKeyBindings();
    }
    
    private void initializeDefaultKeyBindings() {
        // Gizmo operation bindings
        keyBindings.put(GLFW.GLFW_KEY_T, () -> stateManager.updateGizmoOperation(1)); // TRANSLATE
        keyBindings.put(GLFW.GLFW_KEY_R, () -> stateManager.updateGizmoOperation(2)); // ROTATE  
        keyBindings.put(GLFW.GLFW_KEY_S, () -> stateManager.updateGizmoOperation(4)); // SCALE
        keyBindings.put(GLFW.GLFW_KEY_LEFT_SHIFT, () -> 
            stateManager.updateSnapMode(!stateManager.getState().isUseSnap()));
    }
    
    public void registerKeyBinding(int key, Runnable action) {
        keyBindings.put(key, action);
    }
    
    public void unregisterKeyBinding(int key) {
        keyBindings.remove(key);
    }
    
    public void handleKeyInput(int key, int action) {
        if (action == GLFW.GLFW_PRESS && keyBindings.containsKey(key)) {
            keyBindings.get(key).run();
        }
    }
    
    public void handleMouseScroll(double dx, double dy) {
        // Only handle scroll when hovering over the editor window
        if (EditorWindow.isHovering()) {
            float currentDistance = stateManager.getState().getCamDistance();
            float newDistance = currentDistance + ((float) -dy * ApplicationConfig.ZOOM_STEP);
            stateManager.getState().setCamDistance(newDistance);
            EditorWindow.setCamDistance(newDistance);
        }
    }
    
    public void handleMouseButton(int button, int action) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
            boolean pressed = action == GLFW.GLFW_PRESS;
            stateManager.getState().setMiddleMousePressed(pressed);
        }
    }
    
    public void handleCursorMove(double deltaX, double deltaY) {
        // Only handle cursor movement when hovering over the editor window and middle mouse is pressed
        if (EditorWindow.isHovering() && stateManager.getState().isMiddleMousePressed()) {
            EditorWindow.cursorMoveCamera(
                (float) (-deltaX * ApplicationConfig.CAMERA_MOVE_DELTA),
                (float) (deltaY * ApplicationConfig.CAMERA_MOVE_DELTA)
            );
        }
    }
}
