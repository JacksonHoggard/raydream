package me.jacksonhoggard.raydream.service;

import me.jacksonhoggard.raydream.config.ApplicationConfig;
import me.jacksonhoggard.raydream.render.Scene;
import me.jacksonhoggard.raydream.util.ProgressListener;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Service responsible for managing render operations.
 * Provides better control over rendering lifecycle and resource management.
 */
public class RenderService {

    private final ExecutorService renderExecutor;
    private volatile Future<?> currentRenderTask;
    private volatile boolean isRendering = false;

    public RenderService() {
        this.renderExecutor = Executors.newFixedThreadPool(ApplicationConfig.DEFAULT_THREAD_COUNT);
    }

    /**
     * Starts rendering a scene asynchronously.
     * @param scene the scene to render
     * @param filename the output filename
     * @param sampleDepth the sample depth for rendering
     * @param bounces the number of light bounces
     * @param numShadowRays the number of shadow rays
     * @param threads the number of threads to use
     * @param progressListener listener for progress updates
     * @return a Future representing the render operation
     */
    public CompletableFuture<Void> renderAsync(Scene scene, String filename, int sampleDepth, int bounces, int numShadowRays, int threads, ProgressListener progressListener) {
        if (isRendering) {
            throw new IllegalStateException("A render operation is already in progress");
        }

        isRendering = true;

        return CompletableFuture.runAsync(() -> {
            try {
                scene.render(filename, sampleDepth, bounces, numShadowRays, threads, progressListener);
            } catch (IOException e) {
                throw new RuntimeException("Render failed", e);
            } finally {
                isRendering = false;
                currentRenderTask = null;
            }
        }, renderExecutor);
    }

    /**
     * Cancels the current render operation if one is running.
     */
    public void cancelRender() {
        if (currentRenderTask != null && !currentRenderTask.isDone()) {
            currentRenderTask.cancel(true);
            isRendering = false;
        }
    }

    /**
     * Checks if a render operation is currently in progress.
     * @return true if rendering is in progress
     */
    public boolean isRendering() {
        return isRendering;
    }

    /**
     * Shuts down the render service and cleans up resources.
     */
    public void shutdown() {
        cancelRender();
        renderExecutor.shutdown();
    }
}
