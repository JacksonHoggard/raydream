package me.jacksonhoggard.raydream.gui;

import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.ImGuiConfigFlags;
import me.jacksonhoggard.raydream.core.ApplicationContext;
import me.jacksonhoggard.raydream.gui.config.LayoutConfig;
import me.jacksonhoggard.raydream.gui.editor.window.*;
import me.jacksonhoggard.raydream.gui.error.GUIErrorHandler;
import me.jacksonhoggard.raydream.gui.imgui.ImGuiImplGl3Wrapper;
import me.jacksonhoggard.raydream.gui.imgui.ImGuiImplGlfwWrapper;
import me.jacksonhoggard.raydream.gui.imgui.ImGuiSetup;
import me.jacksonhoggard.raydream.gui.input.InputManager;
import me.jacksonhoggard.raydream.gui.layout.ResponsiveLayout;
import me.jacksonhoggard.raydream.gui.render.RenderManager;
import me.jacksonhoggard.raydream.gui.resource.ResourceManager;
import me.jacksonhoggard.raydream.gui.state.StateManager;
import me.jacksonhoggard.raydream.gui.window.WindowManager;
import me.jacksonhoggard.raydream.util.Logger;
import org.lwjgl.glfw.GLFWScrollCallback;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class GUIApplication implements AutoCloseable {
    // Remove unused field
    @SuppressWarnings("unused")
    private final ApplicationContext context;
    private final Logger logger;
    private final ResourceManager resourceManager;
    private final GUIErrorHandler errorHandler;
    private final StateManager stateManager;
    private final WindowManager windowManager;
    private final InputManager inputManager;
    private final RenderManager renderManager;
    
    private ImGuiSetup imGuiSetup;
    private ImGuiImplGlfwWrapper imGuiGlfw;
    private ImGuiImplGl3Wrapper imGuiGl3;
    
    // Mouse state tracking for custom input handling
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private boolean firstMouse = true;
    private boolean lastMiddleMouseState = false;
    
    public GUIApplication(ApplicationContext context) {
        this.context = context;
        this.logger = context.getLoggingService().getLogger(GUIApplication.class);
        this.resourceManager = new ResourceManager(logger);
        this.errorHandler = new GUIErrorHandler(logger);
        this.stateManager = new StateManager();
        this.windowManager = new WindowManager(logger, errorHandler);
        this.inputManager = new InputManager(stateManager);
        this.renderManager = new RenderManager(logger);
    }
    
    public void initialize() {
        try {
            logger.info("Initializing GUI application...");
            
            // Initialize core components
            long windowPtr = resourceManager.register(windowManager).initialize();
            resourceManager.register(renderManager);
            renderManager.initialize();
            
            // Initialize ImGui
            initializeImGui(windowPtr);
            
            // Set up scroll callback for both ImGui and custom input
            setupScrollCallback(windowPtr);
            
            // Initialize state
            initializeState();
            
            logger.info("GUI application initialized successfully");
        } catch (Exception e) {
            errorHandler.handleCriticalError("GUI initialization failed", e);
            throw new RuntimeException("Failed to initialize GUI application", e);
        }
    }
    
    private void initializeImGui(long windowPtr) {
        imGuiSetup = resourceManager.register(new ImGuiSetup(windowPtr, logger));
        imGuiSetup.initialize();
        
        imGuiGlfw = resourceManager.register(new ImGuiImplGlfwWrapper());
        imGuiGl3 = resourceManager.register(new ImGuiImplGl3Wrapper());
        
        imGuiGlfw.init(windowPtr, false); // Don't let ImGui install callbacks, we'll handle them manually
        imGuiGl3.init(windowManager.getGlslVersion());
    }
    
    private void setupScrollCallback(long windowPtr) {
        glfwSetScrollCallback(windowPtr, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double dx, double dy) {
                // Forward to ImGui manually by setting the mouse wheel delta
                // ImGui will read these values in the next frame
                ImGui.getIO().setMouseWheelH(ImGui.getIO().getMouseWheelH() + (float) dx);
                ImGui.getIO().setMouseWheel(ImGui.getIO().getMouseWheel() + (float) dy);
                
                // Handle custom scroll if the editor window is being hovered
                if (me.jacksonhoggard.raydream.gui.editor.window.EditorWindow.isHovering()) {
                    inputManager.handleMouseScroll(dx, dy);
                }
            }
        });
    }
    
    private void initializeState() {
        stateManager.getState().setCamDistance(me.jacksonhoggard.raydream.config.ApplicationConfig.DEFAULT_CAMERA_DISTANCE);
        stateManager.getState().setFps(0);
    }
    
    private void handleCustomInput() {
        // Always check mouse input, but only process it when over editor window
        long window = windowManager.getWindowPtr();
        
        // Handle mouse button state changes
        boolean currentMiddleMouseState = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_MIDDLE) == GLFW_PRESS;
        if (currentMiddleMouseState != lastMiddleMouseState) {
            inputManager.handleMouseButton(GLFW_MOUSE_BUTTON_MIDDLE, 
                currentMiddleMouseState ? GLFW_PRESS : GLFW_RELEASE);
            lastMiddleMouseState = currentMiddleMouseState;
        }
        
        // Handle mouse movement
        double[] xpos = new double[1];
        double[] ypos = new double[1];
        glfwGetCursorPos(window, xpos, ypos);
        
        if (firstMouse) {
            lastMouseX = xpos[0];
            lastMouseY = ypos[0];
            firstMouse = false;
        }
        
        double deltaX = xpos[0] - lastMouseX;
        double deltaY = ypos[0] - lastMouseY;
        lastMouseX = xpos[0];
        lastMouseY = ypos[0];
        
        // Handle mouse movement for camera controls - only if there's actual movement
        if ((deltaX != 0 || deltaY != 0) && currentMiddleMouseState) {
            inputManager.handleCursorMove(deltaX, deltaY);
        }
    }
    
    public void run() {
        logger.info("Starting GUI main loop...");
        
        Timer.calculateDeltaTime(); // Initialize timer
        
        while (!windowManager.shouldClose()) {
            try {
                Timer.calculateDeltaTime();
                updateFPS();
                
                // Clear screen
                glClearColor(0.1F, 0.09F, 0.1F, 1.0F);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                
                // Start ImGui frame
                imGuiGlfw.newFrame();
                ImGui.newFrame();
                
                // Handle custom input when ImGui doesn't want to capture it
                handleCustomInput();
                
                // Render GUI
                renderGUI();
                
                // Render 3D content
                render3DContent();
                
                // Finish ImGui frame
                ImGui.render();
                imGuiGl3.renderDrawData(ImGui.getDrawData());
                
                handleViewports();
                
                windowManager.swapBuffers();
                windowManager.pollEvents();
                
            } catch (Exception e) {
                errorHandler.handleError("Main loop error", e);
            }
        }
        
        logger.info("GUI main loop ended");
    }
    
    private void updateFPS() {
        var state = stateManager.getState();
        state.setFpsTimer(state.getFpsTimer() + Timer.getDeltaTime());
        
        if (state.getFpsTimer() > LayoutConfig.FPS_UPDATE_INTERVAL) {
            state.setFpsTimer(0);
            windowManager.setTitle("RayDream - " + state.getFps() + " FPS");
            state.resetFps();
        }
        
        state.incrementFps();
    }
    
    private void renderGUI() {
        // Calculate responsive layout (will be used in future window refactoring)
        @SuppressWarnings("unused")
        ResponsiveLayout layout = new ResponsiveLayout(
            ImGui.getMainViewport().getSizeX(),
            ImGui.getMainViewport().getSizeY(),
            MenuBar.getHeight()
        );
        
        // Render all windows
        MenuBar.show(null); // Window reference not needed for now
        PropWindow.show();
        EditorWindow.show(renderManager.getEditorFrameBuffer());
        ObjectWindow.show();
        SettingsWindow.show();
        PreviewWindow.show(renderManager.getPreviewFrameBuffer());
    }
    
    private void render3DContent() {
        double currentTime = glfwGetTime();
        
        renderManager.render(
            currentTime,
            EditorWindow.getCamera(),
            PreviewWindow.getCamera(),
            EditorWindow.getWidth(),
            EditorWindow.getHeight(),
            PreviewWindow.getFrameWidth(),
            PreviewWindow.getFrameHeight()
        );
    }
    
    private void handleViewports() {
        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            final long backupWindowPtr = glfwGetCurrentContext();
            ImGui.updatePlatformWindows();
            ImGui.renderPlatformWindowsDefault();
            glfwMakeContextCurrent(backupWindowPtr);
        }
    }
    
    public void shutdown() {
        windowManager.setShouldClose(true);
    }
    
    public ImGuiSetup getImGuiSetup() {
        return imGuiSetup;
    }
    
    // Getters for compatibility with existing window classes
    public float getScale() {
        return imGuiSetup != null ? imGuiSetup.getScale() : 1.0f;
    }
    
    public static ImFont getTitleFont() {
        // This is a temporary bridge - we'll need to refactor the static access
        return Window.getTitleFont();
    }
    
    public static ImFont getBodyFont() {
        // This is a temporary bridge - we'll need to refactor the static access  
        return Window.getBodyFont();
    }
    
    public StateManager getStateManager() {
        return stateManager;
    }
    
    public InputManager getInputManager() {
        return inputManager;
    }
    
    @Override
    public void close() {
        logger.info("Shutting down GUI application...");
        
        try {
            // Stop the main loop gracefully
            if (windowManager != null) {
                windowManager.setShouldClose(true);
            }
            
            // Cleanup EditorObjects first (they may have OpenGL resources)
            cleanupEditorObjects();
            
            // Cleanup static editor resources
            cleanupStaticResources();
            
            // Cleanup callbacks (but don't free them yet - GLFW will handle this)
            // The callbacks will be freed by Callbacks.glfwFreeCallbacks in WindowManager
            
            // Close all managed resources (this includes ImGui, shaders, framebuffers)
            // This must happen BEFORE window/GLFW cleanup
            resourceManager.close();
            
            logger.info("GUI application shutdown complete");
            
        } catch (Exception e) {
            logger.error("Error during GUI application shutdown", e);
        }
    }
    
    private void cleanupEditorObjects() {
        try {
            for (me.jacksonhoggard.raydream.gui.editor.object.EditorObject object : ObjectWindow.objects) {
                object.remove();
            }
        } catch (Exception e) {
            logger.error("Error cleaning up editor objects", e);
        }
    }
    
    private void cleanupStaticResources() {
        try {
            me.jacksonhoggard.raydream.gui.editor.light.EditorAreaLight.cleanup();
            me.jacksonhoggard.raydream.gui.editor.light.EditorPointLight.cleanup();
            me.jacksonhoggard.raydream.gui.editor.light.EditorSphereLight.cleanup();
            me.jacksonhoggard.raydream.gui.editor.object.BoxEditorObject.cleanup();
            me.jacksonhoggard.raydream.gui.editor.object.PlaneEditorObject.cleanup();
            me.jacksonhoggard.raydream.gui.editor.object.SphereEditorObject.cleanup();
            me.jacksonhoggard.raydream.gui.editor.EditorCamera.getModel().remove();
        } catch (Exception e) {
            logger.error("Error cleaning up static resources", e);
        }
    }
}
