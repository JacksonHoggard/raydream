package me.jacksonhoggard.raydream.core.exception;

/**
 * Base exception class for all RayDream-specific exceptions.
 */
public class RayDreamException extends Exception {

    public RayDreamException(String message) {
        super(message);
    }

    public RayDreamException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Exception thrown when scene operations fail.
 */
class SceneException extends RayDreamException {

    public SceneException(String message) {
        super(message);
    }

    public SceneException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Exception thrown when render operations fail.
 */
class RenderException extends RayDreamException {

    public RenderException(String message) {
        super(message);
    }

    public RenderException(String message, Throwable cause) {
        super(message, cause);
    }
}

/**
 * Exception thrown when resource loading fails.
 */
class ResourceException extends RayDreamException {

    public ResourceException(String message) {
        super(message);
    }

    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
