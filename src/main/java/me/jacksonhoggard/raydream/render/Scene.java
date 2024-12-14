package me.jacksonhoggard.raydream.render;

import me.jacksonhoggard.raydream.light.Light;
import me.jacksonhoggard.raydream.light.PointLight;
import me.jacksonhoggard.raydream.material.*;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.*;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.util.ProgressListener;
import me.jacksonhoggard.raydream.util.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Scene {
    private final Camera camera;
    private final Light ambient;
    private final Light[] lights;
    private final Object[] objects;
    private final Vector3D skyColor;
    private final BVH bvh;
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

    public Scene(Camera camera, Light ambient, Light[] lights, Object[] objects, Vector3D skyColor, int width, int height) {
        this.camera = camera;
        this.ambient = ambient;
        this.lights = lights;
        this.objects = objects;
        this.skyColor = skyColor;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.width = width;
        this.height = height;
        this.threadCounter = width * height;
        this.renderProgress = 0;
        this.bvh = new BVH(objects);
    }

    public void render(String filename, int sampleDepth, int bounces, int numShadowRays, int threads, ProgressListener listener) throws IOException {
        progressListener = listener;
        long startTime = System.nanoTime();

        renderCancelListener.setCanceled(false);
        pool = Executors.newFixedThreadPool(threads);
        List<Vector3D> pixelColors = new ArrayList<>();

        for(int j = 0; j < height; j++) {
            for(int i = 0; i < width; i++) {
                pixelColors.add(new Vector3D());
                pool.execute(new TraceRayTask(pixelColors.getLast(), bounces, sampleDepth, numShadowRays, i, j));
            }
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread shutdown interrupted:", e);
        }

        if(renderCancelListener.isCanceled()) {
            System.out.println("Render cancelled.");
            return;
        }

        int k = 0;
        for(int j = 0; j < height; j++) {
            for(int i = 0; i < width; i++) {
                Vector3D pixelColor = pixelColors.get(k++);
                image.setRGB(i, j, new Color((int) Math.min(pixelColor.x * 255, 255), (int) Math.min(pixelColor.y * 255, 255), (int) Math.min(pixelColor.z * 255, 255)).getRGB());
            }
        }
        File output = new File(filename);
        ImageIO.write(image, "png", output);

        long duration = System.nanoTime() - startTime;
        double durationSeconds = ((int) ((duration / 1e9D) * 100) / 100.0);
        int minutes =  (int) (durationSeconds / 60);
        if(minutes > 0)
            System.out.print("\nFinished in " + minutes + "m " + (float) (durationSeconds - (minutes * 60)) + "s");
        else
            System.out.print("\nFinished in " + durationSeconds + "s");
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
            lock.lock();
            threadCounter--;
            double progress = (((((width * height) - threadCounter) / (double) (width * height))) * 100);
            if(renderProgress < (int) progress) {
                renderProgress = (int) progress;
                progressListener.progressUpdated(renderProgress);
            }
            lock.unlock();
        }

        private void takeSamples() {
            camera.shootRay(ray, i, j, 0.5D, -0.5D);
            trace(ray, bounces, pixelColor);
            if(sampleDepth == 1)
                return;
            Vector3D temp = new Vector3D(pixelColor);
            camera.shootRay(ray, i, j, Util.randomRange(0, 1), Util.randomRange(-1, 0));
            trace(ray, bounces, pixelColor);
            samples = 2;
            if(Vector3D.div(pixelColor, samples).equals(temp)) {
                pixelColor.div(samples);
                return;
            }
            for(int k = sampleDepth - samples; k > 0; k--) {
                camera.shootRay(ray, i, j, Util.randomRange(0, 1), Util.randomRange(-1, 0));
                trace(ray, bounces, pixelColor);
                samples++;
            }
            pixelColor.div(samples);
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
            // Find intersections
            Hit bvhHit = bvh.intersect(ray, objects);
            Vector3D pointHit = bvhHit.point();
            Object objectHit = bvhHit.object();
            Vector3D normalHit = bvhHit.normal();
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
            if(objectHit != null && minLightDist < bvhHit.t()) {
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
            Vector3D phongNormal = new Vector3D(normalHit);
            // apply bump map if exists
            if(material.getBumpMap() != null) {
                Vector3D tangent;
                Vector3D bitangent;
                if(bvhHit.triangle() != null) {
                    tangent = bvhHit.triangle().getTangent();
                    bitangent = bvhHit.triangle().getBitangent(normalHit);
                } else {
                    tangent = objectHit.calcTangent(normalHit);
                    bitangent = objectHit.calcBitangent(normalHit, tangent);
                }
                phongNormal.set(material.getBumpMap().apply(normalHit, tangent, bitangent, bvhHit.texCoord()));
            }
            normalHit.set(Object.transformNormalToWS(normalHit, objectHit.getNormalMatrix()));
            phongNormal.set(Object.transformNormalToWS(phongNormal, objectHit.getNormalMatrix()));
            Vector3D reflectionColor = new Vector3D();
            Vector3D refractionColor = new Vector3D();
            switch(material.getType()) {
                case REFLECT -> {
                    double kr = material.fresnelMetal(ray, phongNormal);
                    trace(material.reflectRay(ray, pointHit, normalHit), bounce - 1, reflectionColor);
                    Vector3D phong = new Vector3D();
                    phong(phong, ray, objectHit, pointHit, phongNormal, bvhHit.texCoord());
                    color.add(phong.add(Vector3D.mult(reflectionColor, kr)));
                    return;
                }
                case REFLECT_REFRACT -> {
                    double kr = material.fresnelDielectric(ray, phongNormal);
                    Ray reflectionRay = material.reflectRay(ray, pointHit, normalHit);
                    Ray refractionRay = material.refractRay(ray, pointHit, normalHit);
                    trace(reflectionRay, bounce - 1, reflectionColor);
                    trace(refractionRay, bounce - 1, refractionColor);
                    Vector3D phong = new Vector3D();
                    phong(phong, ray, objectHit, pointHit, phongNormal, bvhHit.texCoord());
                    color.add(phong.add(Vector3D.mult(reflectionColor, kr).add(Vector3D.mult(refractionColor, 1 - kr))));
                    return;
                }
            }
            Vector3D phong = new Vector3D();
            phong(phong, ray, objectHit, pointHit, phongNormal, bvhHit.texCoord());
            color.add(phong);
        }

        private void phong(Vector3D phong, Ray ray, Object objectHit, Vector3D pointHit, Vector3D normalHit, Vector2D texCoord) {
            phong.set(Vector3D.mult(objectHit.getMaterial().getColor(texCoord), objectHit.getMaterial().getAmbient()).mult(ambient.getColor()));
            for(Light light : lights) {
                int maxShadowRays = light.getClass().equals(PointLight.class) ? 1 : numShadowRays;
                Vector3D tempColor = new Vector3D();
                int cols = (int) Math.sqrt(maxShadowRays);
                int rows = maxShadowRays / cols;
                for(int j = 0; j < rows; j++) {
                    for(int i = 0; i < cols; i++) {
                        Vector3D shadowDir = Vector3D.sub(light.pointOnLight(i, j, cols, rows), pointHit).normalize();
                        Ray shadowRay = new Ray(Vector3D.add(pointHit, Vector3D.mult(shadowDir, 0.00001D)), shadowDir);
                        double lightDist = light.intersect(shadowRay);
                        if(lightDist < 0)
                            continue;
                        if(!bvh.intersectShadowRay(shadowRay, objects, lightDist)) {
                            shadowPhong(tempColor, ray, objectHit, shadowRay, pointHit, normalHit, light, lightDist, texCoord);
                        }
                    }
                }
                tempColor.div(rows * cols);
                phong.add(tempColor);
            }
        }

        private static void shadowPhong(Vector3D shadowPhong, Ray ray, Object objectHit, Ray shadowRay, Vector3D pointHit, Vector3D normalHit, Light light, double lightDist, Vector2D texCoord) {
            Ray reflectedRay = objectHit.getMaterial().reflectRay(shadowRay, pointHit, normalHit);
            double kl = Math.max(0D, normalHit.dot(shadowRay.direction().normalized())) * objectHit.getMaterial().getLambertian();
            double ks = Math.pow(Math.max(0, ray.direction().normalized().dot(reflectedRay.direction().normalized())), objectHit.getMaterial().getSpecularExponent()) * objectHit.getMaterial().getSpecular();
            Vector3D s = Vector3D.mult(objectHit.getMaterial().getColor(texCoord), objectHit.getMaterial().getMetalness()).add(new Vector3D(1, 1, 1).mult(1 - objectHit.getMaterial().getMetalness()));
            Vector3D diffuse = new Vector3D(objectHit.getMaterial().getColor(texCoord)).mult(light.getColor()).mult(kl);
            Vector3D specular = new Vector3D(light.getColor()).mult(s).mult(ks);
            double brightness = light.getBrightness() / lightDist;
            (diffuse.add(specular)).mult(brightness);
            shadowPhong.add(diffuse);
        }
    }
}
