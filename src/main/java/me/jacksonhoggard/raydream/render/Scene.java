package me.jacksonhoggard.raydream.render;

import me.jacksonhoggard.raydream.light.Light;
import me.jacksonhoggard.raydream.light.PointLight;
import me.jacksonhoggard.raydream.material.*;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.*;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.util.ProgressListener;
import me.jacksonhoggard.raydream.util.io.SceneReader;
import me.jacksonhoggard.raydream.util.io.SceneWriter;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for(int j = 0; j < height; j++) {
            for(int i = 0; i < width; i++) {
                pool.execute(new TraceRayTask(bounces, sampleDepth, numShadowRays, i, j));
            }
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    public void save(String path) {
        SceneWriter sceneWriter = new SceneWriter();
        sceneWriter.write(this, path);
    }

    public static Scene read(String path) {
        SceneReader sceneReader = new SceneReader();
        return sceneReader.read(path);
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

    private class TraceRayTask implements Runnable {
        private final int bounces;
        private final int sampleDepth;
        private int samples;
        private int numShadowRays;
        private Ray ray;
        private int reusedRayIdx;
        private final Vector3D reusedColor;
        private final int i, j;

        public TraceRayTask(int bounces, int sampleDepth, int numShadowRays, int i, int j) {
            this.bounces = bounces;
            this.sampleDepth = sampleDepth;
            this.i = i;
            this.j = j;
            this.samples = 0;
            this.numShadowRays = numShadowRays;
            this.reusedRayIdx = -1;
            this.ray = new Ray(new Vector3D(), new Vector3D());
            this.reusedColor = new Vector3D();
        }

        public void run() {
            Vector3D totalColor = new Vector3D(0, 0, 0);
            totalColor.add(takeSamples(sampleDepth, 0, 0, 1, -1));
            totalColor.div(samples);
            image.setRGB(i, j, new Color((int) Math.min(totalColor.x * 255, 255), (int) Math.min(totalColor.y * 255, 255), (int) Math.min(totalColor.z * 255, 255)).getRGB());
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

        private Vector3D takeSamples(int sampleDepth, double tlx, double tly, double brx, double bry) {
            // Return black if maximum sample depth reached
            if(sampleDepth <= 0)
                return new Vector3D(0, 0, 0);
            double w = brx - tlx;
            double h = tly - bry;
            Vector3D colorTL;
            Vector3D colorTR;
            Vector3D colorBL;
            Vector3D colorBR;
            if(reusedRayIdx != 0) {
                ray = camera.shootRay(ray, i, j, tlx, tly);
                colorTL = trace(ray, bounces);
            } else {
                colorTL = reusedColor;
            }
            if(reusedRayIdx != 1) {
                ray = camera.shootRay(ray, i, j, tlx + w, bry + h);
                colorTR = trace(ray, bounces);
            } else {
                colorTR = reusedColor;
            }
            if(reusedRayIdx != 2) {
                ray = camera.shootRay(ray, i, j, tlx, bry);
                colorBL = trace(ray, bounces);
            } else {
                colorBL = reusedColor;
            }
            if(reusedRayIdx != 3) {
                ray = camera.shootRay(ray, i, j, brx, bry);
                colorBR = trace(ray, bounces);
            } else {
                colorBR = reusedColor;
            }
            Vector3D totalColor = Vector3D.add(colorTL, colorTR).add(colorBL).add(colorBR).div(4);
            samples++;
            if(colorTL.distance(totalColor) > 0.01D || colorTR.distance(totalColor) > 0.01D || colorBL.distance(totalColor) > 0.01D || colorBR.distance(totalColor) > 0.01D) {
                reusedRayIdx = 0;
                reusedColor.set(colorTR);
                totalColor.add(takeSamples(sampleDepth - 1, tlx, tly, brx - (w / 2), bry + (h / 2)));
                reusedRayIdx = 1;
                reusedColor.set(colorTR);
                totalColor.add(takeSamples(sampleDepth - 1, tlx + (w / 2), tly, brx, bry + (h / 2)));
                reusedRayIdx = 2;
                reusedColor.set(colorBL);
                totalColor.add(takeSamples(sampleDepth - 1, tlx, tly - (h / 2), brx - (w / 2), bry));
                reusedRayIdx = 3;
                reusedColor.set(colorBR);
                totalColor.add(takeSamples(sampleDepth - 1, tlx + (w / 2), tly - (h / 2), brx, bry));
            }
            return totalColor;
        }

        /**
         * Recursive function that traces the path of a primary ray and returns the color of a given ray
         * @param ray current ray
         * @param bounce current bounce
         */
        private Vector3D trace(Ray ray, int bounce) {
            // Return if no more bounces
            if(bounce <= 0)
                return new Vector3D(0, 0, 0);
            // Find intersections
            Hit bvhHit = bvh.intersect(ray, objects);
            Vector3D pointHit = bvhHit.point();
            Object objectHit = bvhHit.object();
            Vector3D normalHit = bvhHit.normal();
            double minLightDist = Double.MAX_VALUE;
            Vector3D minLightColor = null;
            for(Light light : lights) {
                double lightDist = light.intersect(ray);
                if(lightDist > 0.0D && lightDist < minLightDist) {
                    minLightDist = lightDist;
                    minLightColor = light.getColor();
                }
            }
            // Check if light is hit before an object
            if(objectHit != null && minLightDist < bvhHit.t()) {
                // If no light is hit, return sky, else return the color of the light
                return minLightColor;
            }
            // If a light is hit, but no object is hit
            if(objectHit == null && minLightColor != null) {
                return minLightColor;
            }
            // If no object or light is hit
            if(objectHit == null) {
                return skyColor;
            }
            Material material = objectHit.getMaterial();
            Vector3D reflectionColor = new Vector3D();
            Vector3D refractionColor = new Vector3D();
            switch(material.getType()) {
                case REFLECT -> {
                    double kr = material.fresnelMetal(ray, normalHit);
                    reflectionColor.set(trace(material.reflectRay(ray, pointHit, normalHit), bounce - 1));
                    if(!material.hasColor())
                        objectHit.getMaterial().getColor(objectHit, pointHit).set(Vector3D.mult(reflectionColor, kr));
                    return phong(ray, objectHit, pointHit, normalHit).add(Vector3D.mult(reflectionColor, kr));
                }
                case REFLECT_REFRACT -> {
                    double kr = material.fresnelDielectric(ray, normalHit);
                    Ray reflectionRay = material.reflectRay(ray, pointHit, normalHit);
                    Ray refractionRay = material.refractRay(ray, pointHit, normalHit);
                    reflectionColor.set(trace(reflectionRay, bounce - 1));
                    refractionColor.set(trace(refractionRay, bounce - 1));
                    if(!material.hasColor()) objectHit.getMaterial().getColor(objectHit, pointHit).set(Vector3D.mult(reflectionColor, kr).add(Vector3D.mult(refractionColor, 1 - kr)));
                    return phong(ray, objectHit, pointHit, normalHit).add(Vector3D.mult(reflectionColor, kr).add(Vector3D.mult(refractionColor, 1 - kr)));
                }
            }
            return phong(ray, objectHit, pointHit, normalHit);
        }

        private Vector3D phong(Ray ray, Object objectHit, Vector3D pointHit, Vector3D normalHit) {
            Vector3D color = Vector3D.mult(objectHit.getMaterial().getColor(objectHit, pointHit), objectHit.getMaterial().getAmbient()).mult(ambient.getColor());
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
                        Hit bvhHit = bvh.intersect(shadowRay, objects);
                        if (bvhHit.object() != null) {
                            if (bvhHit.t() > 0 && bvhHit.t() < lightDist) {
                                continue;
                            }
                        }
                        if(lightDist < 0)
                            continue;
                        tempColor.add(shadowPhong(ray, objectHit, shadowRay, pointHit, normalHit, light, lightDist));
                    }
                }
                tempColor.div(rows * cols);
                color.add(tempColor);
            }
            return color;
        }

        private Vector3D shadowPhong(Ray ray, Object objectHit, Ray shadowRay, Vector3D pointHit, Vector3D normalHit, Light light, double lightDist) {
            Ray reflectedRay = objectHit.getMaterial().reflectRay(shadowRay, pointHit, normalHit);
            double kl = Math.max(0D, normalHit.dot(shadowRay.getDirection().normalized())) * objectHit.getMaterial().getLambertian();
            double ks = Math.pow(Math.max(0, ray.getDirection().normalized().dot(reflectedRay.getDirection().normalized())), objectHit.getMaterial().getSpecularExponent()) * objectHit.getMaterial().getSpecular();
            Vector3D s = Vector3D.mult(objectHit.getMaterial().getColor(objectHit, pointHit), objectHit.getMaterial().getMetalness()).add(new Vector3D(1, 1, 1).mult(1 - objectHit.getMaterial().getMetalness()));
            Vector3D diffuse = new Vector3D(objectHit.getMaterial().getColor(objectHit, pointHit)).mult(light.getColor()).mult(kl);
            Vector3D specular = new Vector3D(light.getColor()).mult(s).mult(ks);
            double brightness = light.getBrightness() / lightDist;
            return Vector3D.add(diffuse, specular).mult(brightness);
        }
    }
}
