package me.jacksonhoggard.raydream.core;

import me.jacksonhoggard.raydream.service.RenderService;
import me.jacksonhoggard.raydream.service.ResourceCacheService;
import me.jacksonhoggard.raydream.service.SceneService;

/**
 * Application context that manages all services and provides dependency injection.
 * This replaces the scattered static dependencies throughout the codebase.
 */
public class ApplicationContext {

    private static ApplicationContext instance;

    private final SceneService sceneService;
    private final RenderService renderService;
    private final ResourceCacheService cacheService;

    private ApplicationContext() {
        this.sceneService = new SceneService();
        this.renderService = new RenderService();
        this.cacheService = new ResourceCacheService();
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
    }
}
