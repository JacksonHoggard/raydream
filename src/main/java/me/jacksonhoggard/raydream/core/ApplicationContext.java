package me.jacksonhoggard.raydream.core;

import me.jacksonhoggard.raydream.config.ApplicationConfig;
import me.jacksonhoggard.raydream.service.LoggingService;
import me.jacksonhoggard.raydream.service.RenderService;
import me.jacksonhoggard.raydream.service.ResourceCacheService;
import me.jacksonhoggard.raydream.service.SceneService;
import me.jacksonhoggard.raydream.util.LogLevel;

/**
 * Application context that manages all services and provides dependency injection.
 * This replaces the scattered static dependencies throughout the codebase.
 */
public class ApplicationContext {

    private static ApplicationContext instance;

    private final LoggingService loggingService;
    private final SceneService sceneService;
    private final RenderService renderService;
    private final ResourceCacheService cacheService;

    private ApplicationContext() {
        // Initialize logging service first so other services can use it
        LogLevel logLevel = parseLogLevel(ApplicationConfig.LOG_LEVEL);
        this.loggingService = new LoggingService(
            logLevel,
            ApplicationConfig.ENABLE_CONSOLE_LOGGING,
            ApplicationConfig.ENABLE_FILE_LOGGING
        );
        
        // Configure colors
        loggingService.setGlobalColorsEnabled(ApplicationConfig.ENABLE_COLORED_LOGGING);
        
        this.sceneService = new SceneService();
        this.renderService = new RenderService();
        this.cacheService = new ResourceCacheService();
    }

    /**
     * Parses the log level from the configuration string.
     * @param levelString the log level string
     * @return the corresponding LogLevel enum
     */
    private LogLevel parseLogLevel(String levelString) {
        try {
            return LogLevel.valueOf(levelString.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid log level '" + levelString + "', defaulting to INFO");
            return LogLevel.INFO;
        }
    }

    /**
     * Gets the singleton instance of the application context.
     * @return the application context instance
     */
    public static synchronized ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    /**
     * Gets the logging service for managing application logging.
     * @return the logging service
     */
    public LoggingService getLoggingService() {
        return loggingService;
    }

    /**
     * Gets the scene service for managing scene operations.
     * @return the scene service
     */
    public SceneService getSceneService() {
        return sceneService;
    }

    /**
     * Gets the render service for managing render operations.
     * @return the render service
     */
    public RenderService getRenderService() {
        return renderService;
    }

    /**
     * Gets the cache service for resource management.
     * @return the cache service
     */
    public ResourceCacheService getCacheService() {
        return cacheService;
    }

    /**
     * Shuts down all services and cleans up resources.
     */
    public void shutdown() {
        renderService.shutdown();
        cacheService.clearCache();
        loggingService.shutdown();
    }
}
