package me.jacksonhoggard.raydream.config;

/**
 * Central configuration class for application constants and settings.
 * This replaces magic numbers scattered throughout the codebase.
 */
public final class ApplicationConfig {

    // Window Configuration
    public static final int DEFAULT_WINDOW_WIDTH = 1280;
    public static final int DEFAULT_WINDOW_HEIGHT = 720;
    public static final String WINDOW_TITLE = "RayDream - Ray Tracer";

    // Rendering Configuration
    public static final int DEFAULT_MAX_BOUNCE_DEPTH = 10;
    public static final double DEFAULT_EPSILON = 1e-6;
    public static final double RAY_OFFSET_EPSILON = 1e-6;
    public static final double GAMMA_CORRECTION = 2.2;
    public static final double ADAPTIVE_SAMPLING_TOLERANCE = 0.01;
    public static final int DEFAULT_THREAD_COUNT = Runtime.getRuntime().availableProcessors();

    // Camera Configuration
    public static final float DEFAULT_FOV = 60.0f;
    public static final float DEFAULT_NEAR_PLANE = 0.1f;
    public static final float DEFAULT_FAR_PLANE = 1000.0f;
    public static final float DEFAULT_CAMERA_DISTANCE = 5.0f;

    // Editor Configuration
    public static final float ZOOM_STEP = 0.1f;
    public static final float CAMERA_MOVE_DELTA = 0.01f;
    public static final float ROTATION_SENSITIVITY = 0.01f;

    // File Extensions
    public static final String PROJECT_EXTENSION = ".dream";
    public static final String[] SUPPORTED_MODEL_FORMATS = {".obj", ".fbx", ".3ds"};
    public static final String[] SUPPORTED_IMAGE_FORMATS = {".png", ".jpg", ".jpeg", ".bmp", ".tga"};

    // Performance Settings
    public static final int BVH_MAX_OBJECTS_PER_LEAF = 4;
    public static final int TEXTURE_CACHE_SIZE = 100;
    public static final int MODEL_CACHE_SIZE = 50;

    // UI Configuration
    public static final String TITLE_FONT = "Inter.ttf";
    public static final String BODY_FONT = "WorkSans.ttf";
    public static final float DEFAULT_FONT_SIZE = 16.0f;
    public static final float TITLE_FONT_SIZE = 24.0f;

    private ApplicationConfig() {
        // Prevent instantiation
    }
}
