package me.jacksonhoggard.raydream.service;

import me.jacksonhoggard.raydream.config.ApplicationConfig;
import me.jacksonhoggard.raydream.material.Texture;
import me.jacksonhoggard.raydream.object.Model;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service for caching frequently accessed resources to improve performance.
 * Uses LRU (Least Recently Used) eviction policy to manage memory usage.
 */
public class ResourceCacheService {

    private final Map<String, Texture> textureCache;
    private final Map<String, Model> modelCache;

    public ResourceCacheService() {
        this.textureCache = createLRUCache(ApplicationConfig.TEXTURE_CACHE_SIZE);
        this.modelCache = createLRUCache(ApplicationConfig.MODEL_CACHE_SIZE);
    }

    /**
     * Gets a texture from cache or loads it if not present.
     * @param texturePath the path to the texture file
     * @return the cached or newly loaded texture
     */
    public Texture getTexture(String texturePath) {
        return textureCache.computeIfAbsent(texturePath, path -> {
            try {
                BufferedImage image = ImageIO.read(new File(path));
                return new Texture(image, path, image.getWidth(), image.getHeight());
            } catch (Exception e) {
                throw new RuntimeException("Failed to load texture: " + path, e);
            }
        });
    }

    /**
     * Gets a model from cache or loads it if not present.
     * @param modelPath the path to the model file
     * @return the cached or newly loaded model
     */
    public Model getModel(String modelPath) {
        return modelCache.computeIfAbsent(modelPath, path -> {
            try {
                // For now, return null to indicate model loading from cache is not supported
                // The application should use direct model loading instead
                return null;
            } catch (Exception e) {
                throw new RuntimeException("Failed to load model: " + path, e);
            }
        });
    }

    /**
     * Clears all cached resources.
     */
    public void clearCache() {
        textureCache.clear();
        modelCache.clear();
    }

    /**
     * Gets cache statistics.
     * @return a string representation of cache statistics
     */
    public String getCacheStats() {
        return String.format("Textures: %d/%d, Models: %d/%d",
            textureCache.size(), ApplicationConfig.TEXTURE_CACHE_SIZE,
            modelCache.size(), ApplicationConfig.MODEL_CACHE_SIZE);
    }

    private <K, V> Map<K, V> createLRUCache(int maxSize) {
        return new LinkedHashMap<K, V>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        };
    }
}
