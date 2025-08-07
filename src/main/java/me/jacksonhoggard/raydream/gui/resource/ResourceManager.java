package me.jacksonhoggard.raydream.gui.resource;

import me.jacksonhoggard.raydream.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResourceManager implements AutoCloseable {
    private final List<AutoCloseable> resources = new ArrayList<>();
    private final Logger logger;
    private boolean closed = false;
    
    public ResourceManager(Logger logger) {
        this.logger = logger;
    }
    
    public <T extends AutoCloseable> T register(T resource) {
        if (closed) {
            throw new IllegalStateException("ResourceManager is already closed");
        }
        if (resource != null) {
            resources.add(resource);
        }
        return resource;
    }
    
    public void unregister(AutoCloseable resource) {
        resources.remove(resource);
    }
    
    @Override
    public void close() {
        if (closed) {
            return;
        }
        
        logger.info("Starting resource cleanup...");
        closed = true;
        
        // Cleanup in reverse order to handle dependencies
        List<AutoCloseable> reversedResources = new ArrayList<>(resources);
        Collections.reverse(reversedResources);
        
        for (AutoCloseable resource : reversedResources) {
            try {
                if (resource != null) {
                    logger.debug("Closing resource: " + resource.getClass().getSimpleName());
                    resource.close();
                }
            } catch (Exception e) {
                logger.error("Error closing resource: " + resource.getClass().getSimpleName(), e);
                // Continue with cleanup even if one resource fails
            }
        }
        
        resources.clear();
        logger.info("Resource cleanup complete");
    }
    
    public boolean isClosed() {
        return closed;
    }
    
    public int getResourceCount() {
        return resources.size();
    }
}
