package me.jacksonhoggard.raydream.gui.editor.window;

import imgui.ImGui;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import me.jacksonhoggard.raydream.gui.MenuBar;
import me.jacksonhoggard.raydream.gui.editor.EditorCamera;
import me.jacksonhoggard.raydream.gui.editor.object.EditorObject;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.render.FrameBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glViewport;

public class EditorWindow {
    private static EditorCamera camera;
    private static final int CAM_DISTANCE = 8;

    private static final float[] IDENTITY_MATRIX = {
            1.f, 0.f, 0.f, 0.f,
            0.f, 1.f, 0.f, 0.f,
            0.f, 0.f, 1.f, 0.f,
            0.f, 0.f, 0.f, 1.f
    };

    private static final float[] VIEW_MANIPULATE_SIZE = new float[]{128f, 128f};

    private static final float[] EMPTY = new float[]{0};

    private static final float[] inputBounds = new float[]{-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f};
    private static final float[] inputBoundsSnap = new float[]{1f, 1f, 1f};


    private static final ImBoolean boundingSize = new ImBoolean(false);

    private static int currentMode = Mode.LOCAL;
    private static int currentGizmoOperation;

    private static boolean firstFrame = true;
    private static final ImBoolean useSnap = new ImBoolean(false);

    private static float width;
    private static float height;
    private static float posX;
    private static float posY;

    public static void show(FrameBuffer frameBuffer) {
        ImGuizmo.beginFrame();

        posX = PropWindow.getWidth();
        posY = MenuBar.getHeight();
        width = ImGui.getMainViewport().getSizeX() / 2;
        height = (ImGui.getMainViewport().getSizeY() - MenuBar.getHeight()) / 2;

        if (firstFrame) {
            camera = new EditorCamera(27, width / height, 0.1f, 100.f);
            float camYAngle = 165.f / 180.f * (float) Math.PI;
            float camXAngle = 32.f / 180.f * (float) Math.PI;
            Vector3D eye = new Vector3D(
                    (float) (Math.cos(camYAngle) * Math.cos(camXAngle) * CAM_DISTANCE),
                    (float) (Math.sin(camXAngle) * CAM_DISTANCE),
                    (float) (Math.sin(camYAngle) * Math.cos(camXAngle) * CAM_DISTANCE)
            );
            Vector3D at = new Vector3D(0.f, 0.f, 0.f);
            Vector3D up = new Vector3D(0.f, 1.f, 0.f);
            camera.updateViewMatrix(eye, at, up);
            firstFrame = false;
        }

        if(PropWindow.getSelectedTab() == PropWindow.TRANSFORM_TAB) {
            if (ImGui.isKeyPressed(GLFW_KEY_T)) {
                currentGizmoOperation = Operation.TRANSLATE;
            } else if (ImGui.isKeyPressed(GLFW_KEY_R)) {
                currentGizmoOperation = Operation.ROTATE;
            } else if (ImGui.isKeyPressed(GLFW_KEY_S)) {
                currentGizmoOperation = Operation.SCALE;
            } else if (ImGui.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
                useSnap.set(!useSnap.get());
            }
        } else {
            currentGizmoOperation = 0;
        }

        ImGui.setNextWindowPos(posX, posY, ImGuiCond.Always);
        ImGui.setNextWindowSize(width, height, ImGuiCond.Always);
        ImGui.begin("Scene Editor", new ImBoolean(true), ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoBringToFrontOnFocus);
        camera.setAspect(ImGui.getWindowWidth() / ImGui.getWindowHeight());
        camera.updateProjection();

        ImGuizmo.setOrthographic(false);
        ImGuizmo.setEnabled(true);
        ImGuizmo.setDrawList();

        float windowWidth = ImGui.getWindowWidth();
        float windowHeight = ImGui.getWindowHeight();
        ImGuizmo.setRect(ImGui.getWindowPosX(), ImGui.getWindowPosY(), windowWidth, windowHeight);
        ImGuizmo.setId(0);
        ImGuizmo.drawGrid(camera.getViewMatrix().getMatrixArray(), camera.getProjectionMatrix().getMatrixArray(), IDENTITY_MATRIX, 100);

        frameBuffer.rescale((int) windowWidth, (int) windowHeight);
        ImGui.getWindowDrawList().addImage(
                frameBuffer.getTextureId(),
                posX, posY,
                posX + windowWidth, posY + windowHeight,
                0, 1,
                1, 0
        );

        EditorObject selectedObject = ObjectWindow.getSelectedObject();
        if(selectedObject != null) {
            if (useSnap.get() && boundingSize.get() && PropWindow.isBoundSizingSnap()) {
                ImGuizmo.manipulate(camera.getViewMatrix().getMatrixArray(), camera.getProjectionMatrix().getMatrixArray(), selectedObject.getModelMatrix(), currentGizmoOperation, currentMode, PropWindow.getInputSnapValue(), inputBounds, inputBoundsSnap);
            } else if (useSnap.get() && boundingSize.get()) {
                ImGuizmo.manipulate(camera.getViewMatrix().getMatrixArray(), camera.getProjectionMatrix().getMatrixArray(), selectedObject.getModelMatrix(), currentGizmoOperation, currentMode, PropWindow.getInputSnapValue(), inputBounds);
            } else if (boundingSize.get() && PropWindow.isBoundSizingSnap()) {
                ImGuizmo.manipulate(camera.getViewMatrix().getMatrixArray(), camera.getProjectionMatrix().getMatrixArray(), selectedObject.getModelMatrix(), currentGizmoOperation, currentMode, EMPTY, inputBounds, inputBoundsSnap);
            } else if (boundingSize.get()) {
                ImGuizmo.manipulate(camera.getViewMatrix().getMatrixArray(), camera.getProjectionMatrix().getMatrixArray(), selectedObject.getModelMatrix(), currentGizmoOperation, currentMode, EMPTY, inputBounds);
            } else if (useSnap.get()) {
                ImGuizmo.manipulate(camera.getViewMatrix().getMatrixArray(), camera.getProjectionMatrix().getMatrixArray(), selectedObject.getModelMatrix(), currentGizmoOperation, currentMode, PropWindow.getInputSnapValue());
            } else {
                ImGuizmo.manipulate(camera.getViewMatrix().getMatrixArray(), camera.getProjectionMatrix().getMatrixArray(), selectedObject.getModelMatrix(), currentGizmoOperation, currentMode);
            }
        }

        float viewManipulateRight = ImGui.getWindowPosX() + windowWidth;
        float viewManipulateTop = ImGui.getWindowPosY();
        ImGuizmo.viewManipulate(camera.getViewMatrix().getMatrixArray(), CAM_DISTANCE, new float[]{viewManipulateRight - 128, viewManipulateTop}, VIEW_MANIPULATE_SIZE, 0x10101010);

        ImGui.end();
    }

    public static float[] getInputBoundsSnap() {
        return inputBoundsSnap;
    }

    public static ImBoolean getBoundingSize() {
        return boundingSize;
    }

    public static ImBoolean getUseSnap() {
        return useSnap;
    }

    public static void setCurrentMode(int currentMode) {
        EditorWindow.currentMode = currentMode;
    }

    public static int getCurrentMode() {
        return currentMode;
    }

    public static int getCurrentGizmoOperation() {
        return currentGizmoOperation;
    }

    public static EditorCamera getCamera() {
        return camera;
    }

    public static float getWidth() {
        return width;
    }

    public static float getHeight() {
        return height;
    }

    public static float getPosX() {
        return posX;
    }

    public static float getPosY() {
        return posY;
    }
}
