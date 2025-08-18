package me.jacksonhoggard.raydream.gui.window;

import me.jacksonhoggard.raydream.gui.error.GUIErrorHandler;
import me.jacksonhoggard.raydream.util.Logger;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class WindowManager implements AutoCloseable {
    private final Logger logger;
    private final GUIErrorHandler errorHandler;
    private long windowPtr;
    private String glslVersion;
    private GLFWErrorCallback errorCallback;
    
    public WindowManager(Logger logger, GUIErrorHandler errorHandler) {
        this.logger = logger;
        this.errorHandler = errorHandler;
    }
    
    public long initialize() {
        try {
            initializeGLFW();
            createWindow();
            setupWindow();
            return windowPtr;
        } catch (Exception e) {
            errorHandler.handleCriticalError("Window initialization failed", e);
            throw new RuntimeException("Failed to initialize window", e);
        }
    }
    
    private void initializeGLFW() {
        // Set error callback BEFORE glfwInit
        errorCallback = GLFWErrorCallback.createPrint(System.err);
        glfwSetErrorCallback(errorCallback);
        
        if (!glfwInit()) {
            throw new RuntimeException("Unable to initialize GLFW");
        }
        
        // Initialize GLSL
        glslVersion = "#version 330";
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        
        logger.info("GLFW initialized successfully");
    }
    
    private void createWindow() {
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        
        if (vidMode == null) {
            throw new RuntimeException("Unable to get video mode");
        }
        
        windowPtr = glfwCreateWindow(
            (int) (vidMode.width() * 0.8D), 
            (int) (vidMode.height() * 0.8D), 
            "RayDream", 
            NULL, 
            NULL
        );
        
        if (windowPtr == NULL) {
            throw new RuntimeException("Unable to create window");
        }
        
        logger.info("Window created successfully");
    }
    
    private void setupWindow() {
        setWindowIcon();
        configureWindow();
        
        glfwMakeContextCurrent(windowPtr);
        glfwSwapInterval(1);
        glfwShowWindow(windowPtr);
        
        GL.createCapabilities();
    }
    
    private void setWindowIcon() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            String iconPath = "logo.png";
            InputStream iconStream = ClassLoader.getSystemResourceAsStream(iconPath);
            if (iconStream == null) {
                logger.warn("Icon file not found: " + iconPath);
                return;
            }
            
            ByteBuffer iconBuffer = ByteBuffer.allocateDirect(iconStream.available());
            Channels.newChannel(iconStream).read(iconBuffer);
            iconBuffer.flip();
            
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            IntBuffer channelsBuffer = stack.mallocInt(1);
            
            ByteBuffer decodedImage = STBImage.stbi_load_from_memory(
                iconBuffer, widthBuffer, heightBuffer, channelsBuffer, 4
            );
            
            if (decodedImage == null) {
                logger.warn("Failed to load icon: " + STBImage.stbi_failure_reason());
                return;
            }
            
            GLFWImage.Buffer imageBuffer = GLFWImage.malloc(1);
            GLFWImage icon = imageBuffer.get(0);
            icon.set(widthBuffer.get(0), heightBuffer.get(0), decodedImage);
            
            glfwSetWindowIcon(windowPtr, imageBuffer);
            
            STBImage.stbi_image_free(decodedImage);
            imageBuffer.free();
            
        } catch (Exception e) {
            errorHandler.handleWarning("Failed to set window icon", e.getMessage());
        }
    }
    
    private void configureWindow() {
        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        if (vidMode != null) {
            glfwSetWindowAspectRatio(windowPtr, 16, 9);
            glfwSetWindowSizeLimits(
                windowPtr, 
                (int) (vidMode.width() * 0.5D), 
                (int) ((vidMode.width() * 0.5D) / (16.f/9.f)), 
                GLFW_DONT_CARE, 
                GLFW_DONT_CARE
            );
        }
    }
    
    public void setCallbacks(GLFWScrollCallback scrollCallback, 
                           GLFWMouseButtonCallback mouseButtonCallback,
                           GLFWCursorPosCallback cursorPosCallback,
                           GLFWKeyCallback keyCallback,
                           GLFWCharCallback charCallback) {
        glfwSetScrollCallback(windowPtr, scrollCallback);
        glfwSetMouseButtonCallback(windowPtr, mouseButtonCallback);
        glfwSetCursorPosCallback(windowPtr, cursorPosCallback);
        glfwSetKeyCallback(windowPtr, keyCallback);
        glfwSetCharCallback(windowPtr, charCallback);
    }
    
    public boolean shouldClose() {
        return glfwWindowShouldClose(windowPtr);
    }
    
    public void setShouldClose(boolean shouldClose) {
        glfwSetWindowShouldClose(windowPtr, shouldClose);
    }
    
    public void swapBuffers() {
        glfwSwapBuffers(windowPtr);
    }
    
    public void pollEvents() {
        glfwPollEvents();
    }
    
    public void setTitle(String title) {
        glfwSetWindowTitle(windowPtr, title);
    }
    
    public long getWindowPtr() {
        return windowPtr;
    }
    
    public String getGlslVersion() {
        return glslVersion;
    }
    
    @Override
    public void close() {
        logger.info("Starting WindowManager cleanup...");
        
        try {
            if (windowPtr != NULL) {
                // Make sure we're on the right context before cleanup
                glfwMakeContextCurrent(windowPtr);
                
                // Free callbacks first
                Callbacks.glfwFreeCallbacks(windowPtr);
                
                // Destroy window
                glfwDestroyWindow(windowPtr);
                windowPtr = NULL;
                logger.info("Window destroyed");
            }
        } catch (Exception e) {
            logger.error("Error during window cleanup", e);
        }
        
        try {
            // Terminate GLFW last
            glfwTerminate();
            logger.info("GLFW terminated");
        } catch (Exception e) {
            logger.error("Error during GLFW termination", e);
        }
        
        try {
            // Free error callback last
            if (errorCallback != null) {
                errorCallback.free();
                errorCallback = null;
                logger.info("Error callback freed");
            }
        } catch (Exception e) {
            logger.error("Error freeing error callback", e);
        }
        
        logger.info("WindowManager cleanup complete");
    }
}
