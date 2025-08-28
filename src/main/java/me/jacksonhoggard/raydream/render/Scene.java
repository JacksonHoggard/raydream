package me.jacksonhoggard.raydream.render;

import me.jacksonhoggard.raydream.acceleration.ImprovedBVH;
import me.jacksonhoggard.raydream.config.ApplicationConfig;
import me.jacksonhoggard.raydream.core.ApplicationContext;
import me.jacksonhoggard.raydream.light.Light;
import me.jacksonhoggard.raydream.light.PointLight;
import me.jacksonhoggard.raydream.material.*;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.*;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.util.Logger;
import me.jacksonhoggard.raydream.util.ProgressListener;
import me.jacksonhoggard.raydream.util.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Scene {
    private static final Logger logger = ApplicationContext.getInstance().getLoggingService().getLogger(Scene.class);
    
    private final Camera camera;
    private final Light ambient;
    private final double ambientCoefficient;
    private final Light[] lights;
    private final Object[] objects;
    private final Vector3D skyColor;
    private final ImprovedBVH bvh;
    private final BufferedImage image;
    private final int width;
    private final int height;
    private int threadCounter;
    private int renderProgress;
    private ProgressListener progressListener;
    private static ExecutorService pool;
    private static final RenderCancelListener renderCancelListener = new RenderCancelListener() {
        public boolean canceled = false;

        @Override
        public void cancel() {
            canceled = true;
            pool.shutdownNow();
        }

        @Override
        public boolean isCanceled() {
            return canceled;
        }

        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
        }
    };
    private static final Lock lock = new ReentrantLock();

    public Scene(
        Camera camera,
        Light ambient,
        double ambientCoefficient,
        Light[] lights,
        Object[] objects,
        Vector3D skyColor,
        int width,
        int height
    ) {
        this.camera = camera;
        this.ambient = ambient;
        this.ambientCoefficient = ambientCoefficient;
        this.lights = lights;
        this.objects = objects;
        this.skyColor = skyColor;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.width = width;
        this.height = height;
        this.threadCounter = width * height;
        this.renderProgress = 0;
        this.bvh = new ImprovedBVH(Arrays.asList(objects));
    }

    public void render(String filename, int sampleDepth, int bounces, int numShadowRays, int threads, ProgressListener listener) throws IOException {
        progressListener = listener;
        long startTime = System.nanoTime();

        renderCancelListener.setCanceled(false);
        pool = Executors.newFixedThreadPool(threads);
        List<Vector3D> pixelColors = new ArrayList<>();
        List<TraceRayTask> tasks = new ArrayList<>();

        for(int j = 0; j < height; j++) {
            for(int i = 0; i < width; i++) {
                pixelColors.add(new Vector3D());
                tasks.add(new TraceRayTask(pixelColors.getLast(), bounces, sampleDepth, numShadowRays, i, j));
            }
        }
        Collections.shuffle(tasks);
        for(TraceRayTask task : tasks) {
            pool.execute(task);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread shutdown interrupted:", e);
        }

        if(renderCancelListener.isCanceled()) {
            logger.info("Render cancelled by user");
            return;
        }

        int k = 0;
        Vector3D[][] imageData = new Vector3D[height][width];
        
        // First pass: tone mapping and gamma correction
        for(int j = 0; j < height; j++) {
            for(int i = 0; i < width; i++) {
                Vector3D pixelColor = pixelColors.get(k++);
                
                // Apply tone mapping to handle HDR values better
                pixelColor.x = toneMap(pixelColor.x);
                pixelColor.y = toneMap(pixelColor.y);
                pixelColor.z = toneMap(pixelColor.z);
                
                imageData[j][i] = new Vector3D(pixelColor);
            }
        }
        
        // Apply bilateral denoising filter for high DOF scenes
        if(camera.getAperture() > 5.0) {
            imageData = applyBilateralFilter(imageData, width, height);
        }
        
        // Second pass: dithering and final color conversion
        for(int j = 0; j < height; j++) {
            for(int i = 0; i < width; i++) {
                Vector3D pixelColor = imageData[j][i];
                
                // Apply dithering to reduce banding artifacts (configurable)
                if (ApplicationConfig.ENABLE_DITHERING) {
                    pixelColor.x = Util.applyDithering(pixelColor.x, i, j);
                    pixelColor.y = Util.applyDithering(pixelColor.y, i, j);
                    pixelColor.z = Util.applyDithering(pixelColor.z, i, j);
                }
                
                // Apply gamma correction for more accurate color display
                double invGamma = 1.0 / ApplicationConfig.GAMMA_CORRECTION;
                double r = Math.pow(Math.min(Math.max(pixelColor.x, 0.0), 1.0), invGamma) * 255;
                double g = Math.pow(Math.min(Math.max(pixelColor.y, 0.0), 1.0), invGamma) * 255;
                double b = Math.pow(Math.min(Math.max(pixelColor.z, 0.0), 1.0), invGamma) * 255;
                
                // Round to prevent floating point artifacts
                int red = Math.min(255, Math.max(0, (int) Math.round(r)));
                int green = Math.min(255, Math.max(0, (int) Math.round(g)));
                int blue = Math.min(255, Math.max(0, (int) Math.round(b)));
                
                image.setRGB(i, j, new Color(red, green, blue).getRGB());
            }
        }
        File output = new File(filename);
        ImageIO.write(image, "png", output);

        long duration = System.nanoTime() - startTime;
        double durationSeconds = ((int) ((duration / 1e9D) * 100) / 100.0);
        int minutes =  (int) (durationSeconds / 60);
        if(minutes > 0)
            logger.info("Render completed in " + minutes + "m " + (float) (durationSeconds - (minutes * 60)) + "s");
        else
            logger.info("Render completed in " + durationSeconds + "s");
    }

    /**
     * Simple tone mapping to handle HDR values and reduce artifacts
     * Uses Reinhard tone mapping operator
     */
    private double toneMap(double value) {
        // Reinhard tone mapping: x / (1 + x)
        return value / (1.0 + value);
    }

    /**
     * Apply bilateral filtering to reduce noise while preserving edges
     * Particularly effective for depth of field noise
     */
    private Vector3D[][] applyBilateralFilter(Vector3D[][] imageData, int width, int height) {
        Vector3D[][] filtered = new Vector3D[height][width];
        int filterRadius = ApplicationConfig.BILATERAL_FILTER_RADIUS;
        double spatialSigma = ApplicationConfig.BILATERAL_SPATIAL_SIGMA;
        double intensitySigma = ApplicationConfig.BILATERAL_INTENSITY_SIGMA;
        
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                Vector3D centerPixel = imageData[y][x];
                Vector3D filteredColor = new Vector3D();
                double weightSum = 0.0;
                
                // Sample neighbors within filter radius
                for(int dy = -filterRadius; dy <= filterRadius; dy++) {
                    for(int dx = -filterRadius; dx <= filterRadius; dx++) {
                        int ny = Math.max(0, Math.min(height - 1, y + dy));
                        int nx = Math.max(0, Math.min(width - 1, x + dx));
                        Vector3D neighborPixel = imageData[ny][nx];
                        
                        // Spatial weight (based on distance)
                        double spatialDist = Math.sqrt(dx * dx + dy * dy);
                        double spatialWeight = Math.exp(-(spatialDist * spatialDist) / (2 * spatialSigma * spatialSigma));
                        
                        // Intensity weight (based on color similarity) - more aggressive for DOF
                        double intensityDist = Vector3D.sub(centerPixel, neighborPixel).length();
                        double intensityWeight = Math.exp(-(intensityDist * intensityDist) / (2 * intensitySigma * intensitySigma));
                        
                        double totalWeight = spatialWeight * intensityWeight;
                        filteredColor.add(Vector3D.mult(neighborPixel, totalWeight));
                        weightSum += totalWeight;
                    }
                }
                
                if(weightSum > 0) {
                    filtered[y][x] = Vector3D.div(filteredColor, weightSum);
                } else {
                    filtered[y][x] = new Vector3D(centerPixel);
                }
            }
        }
        
        return filtered;
    }

    public Camera getCamera() {
        return camera;
    }

    public Light getAmbient() {
        return ambient;
    }

    public Object[] getObjects() {
        return objects;
    }

    public Light[] getLights() {
        return lights;
    }

    public static RenderCancelListener getRenderCancelListener() {
        return renderCancelListener;
    }

    private class TraceRayTask implements Runnable {
        private final Vector3D pixelColor;
        private final int bounces;
        private final int sampleDepth;
        private int samples;
        private final int numShadowRays;
        private Ray ray;
        private final int i, j;

        public TraceRayTask(Vector3D pixelColor, int bounces, int sampleDepth, int numShadowRays, int i, int j) {
            this.pixelColor = pixelColor;
            this.bounces = bounces;
            this.sampleDepth = sampleDepth;
            this.i = i;
            this.j = j;
            this.samples = 0;
            this.numShadowRays = numShadowRays;
            this.ray = new Ray(new Vector3D(), new Vector3D());
        }

        public void run() {
            takeSamples();
            updateProgress();
        }

        private void updateProgress() {
            // Apply gamma correction for more accurate color display
            double invGamma = 1.0 / ApplicationConfig.GAMMA_CORRECTION;
            double r = Math.pow(Math.min(Math.max(toneMap(pixelColor.x), 0.0), 1.0), invGamma) * 255;
            double g = Math.pow(Math.min(Math.max(toneMap(pixelColor.y), 0.0), 1.0), invGamma) * 255;
            double b = Math.pow(Math.min(Math.max(toneMap(pixelColor.z), 0.0), 1.0), invGamma) * 255;

            // Round to prevent floating point artifacts
            int red = Math.min(255, Math.max(0, (int) Math.round(r)));
            int green = Math.min(255, Math.max(0, (int) Math.round(g)));
            int blue = Math.min(255, Math.max(0, (int) Math.round(b)));
            lock.lock();
            threadCounter--;
            double progress = (((((width * height) - threadCounter) / (double) (width * height))) * 100);
            image.setRGB(i, j, new Color(
                red,
                green,
                blue
            ).getRGB());
            if(renderProgress < (int) progress) {
                renderProgress = (int) progress;
                progressListener.progressUpdated(renderProgress, image);
            }
            lock.unlock();
        }

        private void takeSamples() {
            // Determine if this is a high DOF scene requiring more samples
            boolean isHighDOF = camera.getAperture() > 5.0;
            int maxSamples = isHighDOF ? 
                Math.min(ApplicationConfig.MAX_DOF_SAMPLES, sampleDepth * 2) : sampleDepth;
            
            // First sample at pixel center for base quality
            camera.shootRay(ray, i, j, 0.5D, 0.5D);
            trace(ray, bounces, pixelColor);
            samples = 1;
            
            if(maxSamples == 1)
                return;
                
            // Track variance for better convergence detection
            Vector3D colorSum = new Vector3D(pixelColor);
            Vector3D colorSumSquared = new Vector3D(
                pixelColor.x * pixelColor.x, 
                pixelColor.y * pixelColor.y, 
                pixelColor.z * pixelColor.z
            );
            
            Vector3D tempColor = new Vector3D();
            
            // Use stratified sampling for first batch of samples
            int stratifiedSamples = Math.min(16, maxSamples - 1);
            for(int s = 0; s < stratifiedSamples; s++) {
                tempColor.set(0, 0, 0);
                
                // Stratified sampling within pixel
                double stratumX = (s % 4) * 0.25 + Util.randomRange(0, 0.25);
                double stratumY = (s / 4) * 0.25 + Util.randomRange(0, 0.25);
                
                // Use stratified aperture sampling for high DOF scenes
                if(isHighDOF && stratifiedSamples >= 16) {
                    camera.shootRayStratified(ray, i, j, stratumX, stratumY, s, 16);
                } else {
                    camera.shootRay(ray, i, j, stratumX, stratumY);
                }
                trace(ray, bounces, tempColor);
                
                colorSum.add(tempColor);
                colorSumSquared.add(new Vector3D(
                    tempColor.x * tempColor.x,
                    tempColor.y * tempColor.y, 
                    tempColor.z * tempColor.z
                ));
                samples++;
            }
            
            // Continue with Halton sequence for remaining samples
            for(int sample = samples; sample < maxSamples; sample++) {
                tempColor.set(0, 0, 0);
                
                // Use Halton sequence for better sample distribution
                double jitterX = Util.vanDerCorput(sample, 2);
                double jitterY = Util.vanDerCorput(sample, 3);
                
                camera.shootRay(ray, i, j, jitterX, jitterY);
                trace(ray, bounces, tempColor);
                
                colorSum.add(tempColor);
                colorSumSquared.add(new Vector3D(
                    tempColor.x * tempColor.x,
                    tempColor.y * tempColor.y, 
                    tempColor.z * tempColor.z
                ));
                samples++;
                
                // Check convergence using variance
                if(samples >= ApplicationConfig.MIN_SAMPLES_BEFORE_CONVERGENCE && 
                   samples % 8 == 0) { // Check less frequently to reduce overhead
                    
                    Vector3D mean = Vector3D.div(colorSum, samples);
                    Vector3D meanSquared = Vector3D.div(colorSumSquared, samples);
                    Vector3D variance = Vector3D.sub(meanSquared, new Vector3D(
                        mean.x * mean.x, mean.y * mean.y, mean.z * mean.z
                    ));
                    
                    double totalVariance = variance.x + variance.y + variance.z;
                    double convergenceThreshold = isHighDOF ? 
                        ApplicationConfig.DOF_NOISE_THRESHOLD : 
                        ApplicationConfig.ADAPTIVE_SAMPLING_TOLERANCE;
                    
                    if(totalVariance < convergenceThreshold) {
                        break;
                    }
                }
            }
            
            // Set final pixel color
            pixelColor.set(Vector3D.div(colorSum, samples));
        }

        /**
         * Recursive function that traces the path of a primary ray and returns the color of a given ray
         * @param ray current ray
         * @param bounce current bounce
         * @param color pointer to the color to be calculated by the function
         */
        private void trace(Ray ray, int bounce, Vector3D color) {
            // Return if no more bounces
            if(bounce <= 0)
                return;
            // Find intersections using ImprovedBVH
            Hit bvhHit = bvh.intersect(ray, 0.0001, Double.MAX_VALUE);
            Vector3D pointHit = bvhHit != null ? bvhHit.point() : null;
            Object objectHit = bvhHit != null ? bvhHit.object() : null;
            Vector3D normalHit = bvhHit != null ? bvhHit.normal() : null;
            double minLightDist = Double.MAX_VALUE;
            Vector3D minLightColor = null;
            double minLightBrightness = Double.MAX_VALUE;
            for(Light light : lights) {
                double lightDist = light.intersect(ray);
                if(lightDist > 0.0D && lightDist < minLightDist) {
                    minLightDist = lightDist;
                    minLightColor = light.getColor();
                    minLightBrightness = light.getBrightness();
                }
            }
            // Check if light is hit before an object
            if(objectHit != null && bvhHit != null && minLightDist < bvhHit.t()) {
                // If light is hit return the color of the light
                double brightness = minLightBrightness / minLightDist;
                color.add(Vector3D.mult(minLightColor, brightness));
                return;
            }
            // If a light is hit, but no object is hit
            if(objectHit == null && minLightColor != null) {
                double brightness = minLightBrightness / minLightDist;
                color.add(Vector3D.mult(minLightColor, brightness));
                return;
            }
            // If no object or light is hit
            if(objectHit == null) {
                color.add(skyColor);
                return;
            }
            Material material = objectHit.getMaterial();
            Vector3D shaderNormal = new Vector3D(normalHit);
            Vector3D tangent;
            Vector3D bitangent;
            if(bvhHit.triangle() != null) {
                tangent = bvhHit.triangle().getTangent();
                bitangent = bvhHit.triangle().getBitangent(normalHit);
            } else {
                tangent = objectHit.calcTangent(normalHit);
                bitangent = objectHit.calcBitangent(normalHit, tangent);
            }
            // apply bump map if exists
            if(material.getBumpMap() != null) {
                shaderNormal.set(material.getBumpMap().apply(normalHit, tangent, bitangent, bvhHit.texCoord()));
            }
            normalHit.set(Object.transformNormalToWS(normalHit, objectHit.getNormalMatrix()));
            shaderNormal.set(Object.transformNormalToWS(shaderNormal, objectHit.getNormalMatrix()));
            Vector3D tangentWS = Object.transformNormalToWS(tangent, objectHit.getNormalMatrix());
            Vector3D bitangentWS = Object.transformNormalToWS(bitangent, objectHit.getNormalMatrix());
            Vector3D reflectionColor = new Vector3D();
            Vector3D refractionColor = new Vector3D();
            switch(material.getType()) {
                case REFLECT -> {
                    double kr = material.fresnelMetal(ray, shaderNormal);
                    trace(material.reflectRay(ray, pointHit, normalHit), bounce - 1, reflectionColor);
                    Vector3D shading = new Vector3D();
                    shade(shading, ray, objectHit, pointHit, shaderNormal, bvhHit.texCoord(), tangentWS, bitangentWS);
                    color.add(shading.add(Vector3D.mult(reflectionColor, kr)));
                    return;
                }
                case REFLECT_REFRACT -> {
                    double kr = material.fresnelDielectric(ray, shaderNormal);
                    Ray reflectionRay = material.reflectRay(ray, pointHit, normalHit);
                    Ray refractionRay = material.refractRay(ray, pointHit, normalHit);
                    trace(reflectionRay, bounce - 1, reflectionColor);
                    trace(refractionRay, bounce - 1, refractionColor);
                    Vector3D shading = new Vector3D();
                    shade(shading, ray, objectHit, pointHit, shaderNormal, bvhHit.texCoord(), tangentWS, bitangentWS);
                    color.add(shading.add(Vector3D.mult(reflectionColor, kr).add(Vector3D.mult(refractionColor, 1 - kr))));
                    return;
                }
                case OTHER -> {
                    // Standard diffuse/specular material - no reflection/refraction
                    Vector3D shading = new Vector3D();
                    shade(shading, ray, objectHit, pointHit, shaderNormal, bvhHit.texCoord(), tangentWS, bitangentWS);
                    color.add(shading);
                    return;
                }
            }
            Vector3D shading = new Vector3D();
            shade(shading, ray, objectHit, pointHit, shaderNormal, bvhHit.texCoord(), tangentWS, bitangentWS);
            color.add(shading);
        }

        private void shade(Vector3D out, Ray ray, Object objectHit, Vector3D pointHit, Vector3D normalHit, Vector2D texCoord, Vector3D x, Vector3D y) {
            out.set(Vector3D.mult(objectHit.getMaterial().getAlbedo(texCoord), ambientCoefficient).mult(ambient.getColor()));
            for(Light light : lights) {
                int maxShadowRays = light.getClass().equals(PointLight.class) ? 1 : numShadowRays;
                Vector3D tempColor = new Vector3D();
                int cols = (int) Math.sqrt(maxShadowRays);
                int rows = maxShadowRays / cols;
                int numHits = 0;
                for(int j = 0; j < rows; j++) {
                    for(int i = 0; i < cols; i++) {
                        Vector3D shadowDir = Vector3D.sub(light.pointOnLight(i, j, cols, rows), pointHit).normalize();
                        // Improved shadow ray origin with better bias calculation
                        Vector3D shadowOrigin = Vector3D.add(pointHit, Vector3D.mult(normalHit, ApplicationConfig.RAY_OFFSET_EPSILON));
                        // Add small directional bias to prevent self-intersection
                        shadowOrigin.add(Vector3D.mult(shadowDir, ApplicationConfig.RAY_OFFSET_EPSILON * 0.1));
                        Ray shadowRay = new Ray(shadowOrigin, shadowDir);
                        double lightDist = light.intersect(shadowRay);
                        if(lightDist < 0)
                            continue;
                        if(bvh.intersectShadowRay(shadowRay, lightDist))
                            continue;
                        numHits++;
                    }
                }
                BSDF.brdf(tempColor, ray, objectHit, pointHit, normalHit, texCoord, light, x, y);
                tempColor.mult(numHits / (double) maxShadowRays);
                out.add(tempColor);
            }
        }

    //     private static void shadowPhong(Vector3D shadowPhong, Ray ray, Object objectHit, Ray shadowRay, Vector3D pointHit, Vector3D normalHit, Light light, double lightDist, Vector2D texCoord) {
    //         if (lightDist <= 0) return; // Safety check
            
    //         Ray reflectedRay = objectHit.getMaterial().reflectRay(shadowRay, pointHit, normalHit);
    //         double kl = Math.max(0D, normalHit.dot(shadowRay.direction().normalized())) * objectHit.getMaterial().getLambertian();
    //         double ks = Math.pow(Math.max(0, ray.direction().normalized().dot(reflectedRay.direction().normalized())), objectHit.getMaterial().getSpecularExponent()) * objectHit.getMaterial().getSpecular();
    //         Vector3D s = Vector3D.mult(objectHit.getMaterial().getColor(texCoord), objectHit.getMaterial().getMetalness()).add(new Vector3D(1, 1, 1).mult(1 - objectHit.getMaterial().getMetalness()));
    //         Vector3D diffuse = new Vector3D(objectHit.getMaterial().getColor(texCoord)).mult(light.getColor()).mult(kl);
    //         Vector3D specular = new Vector3D(light.getColor()).mult(s).mult(ks);
    //         double brightness = light.getBrightness() / lightDist;
    //         diffuse.mult(brightness);
    //         specular.mult(brightness);
    //         shadowPhong.add(Vector3D.add(diffuse, specular));
    //     }
    }
}
