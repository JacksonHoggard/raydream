package me.jacksonhoggard.raydream.render;

import me.jacksonhoggard.raydream.acceleration.ImprovedBVH;
import me.jacksonhoggard.raydream.config.ApplicationConfig;
import me.jacksonhoggard.raydream.core.ApplicationContext;
import me.jacksonhoggard.raydream.light.Light;
import me.jacksonhoggard.raydream.material.*;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.*;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.render.BSDF.BSDFSample;
import me.jacksonhoggard.raydream.render.sample.LightSample;
import me.jacksonhoggard.raydream.util.Logger;
import me.jacksonhoggard.raydream.util.MathUtils;
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
    private final Light[] lights;
    private final Object[] objects;
    private final Vector3D skyColor;
    private final int rrStart;
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
            Light[] lights,
            Object[] objects,
            Vector3D skyColor,
            int rrStart,
            int width,
            int height) {
        this.camera = camera;
        this.lights = lights;
        this.objects = objects;
        this.skyColor = skyColor;
        this.rrStart = rrStart;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        this.width = width;
        this.height = height;
        this.threadCounter = width * height;
        this.renderProgress = 0;
        this.bvh = new ImprovedBVH(Arrays.asList(objects));
    }

    public void render(String filename, int sampleDepth, int bounces, int threads, ProgressListener listener)
            throws IOException {
        progressListener = listener;
        long startTime = System.nanoTime();

        renderCancelListener.setCanceled(false);
        pool = Executors.newFixedThreadPool(threads);
        List<Vector3D> pixelColors = new ArrayList<>();
        List<TraceRayTask> tasks = new ArrayList<>();

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                pixelColors.add(new Vector3D());
                tasks.add(new TraceRayTask(pixelColors.getLast(), bounces, sampleDepth, i, j));
            }
        }
        Collections.shuffle(tasks);
        for (TraceRayTask task : tasks) {
            pool.execute(task);
        }
        pool.shutdown();
        try {
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread shutdown interrupted:", e);
        }

        if (renderCancelListener.isCanceled()) {
            logger.info("Render cancelled by user");
            return;
        }

        int k = 0;
        Vector3D[][] imageData = new Vector3D[height][width];

        // First pass: tone mapping and gamma correction
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                Vector3D pixelColor = pixelColors.get(k++);

                // Apply tone mapping to handle HDR values better
                pixelColor.x = toneMap(pixelColor.x);
                pixelColor.y = toneMap(pixelColor.y);
                pixelColor.z = toneMap(pixelColor.z);

                imageData[j][i] = new Vector3D(pixelColor);
            }
        }

        // Second pass: final color conversion
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                Vector3D pixelColor = imageData[j][i];

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
        int minutes = (int) (durationSeconds / 60);
        if (minutes > 0)
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

    public LightSample sampleLight(Vector3D pointHit) {
        // Sample a light source and return its properties
        Light light = getRandomLight();
        if (light == null)
            return null;
        Vector3D lightPoint = light.pointOnLight(0, 0, 1, 1);
        Vector3D lightVec = Vector3D.sub(lightPoint, pointHit); // FROM surface TO light
        Vector3D wi = lightVec.normalized(); // Direction toward light
        Vector3D normalAtLight = light.normalAt(lightPoint);
        double distance = pointHit.distance(lightPoint);
        Vector3D le = Vector3D.mult(light.getColor(), light.getBrightness());
        double pDir = (1.0D / light.getArea()) * (1.0D / lights.length) * (distance * distance)
                / Math.max(1e-9, normalAtLight.dot(wi.negated()));
        Ray shadowRay = new Ray(Vector3D.add(pointHit, Vector3D.mult(wi, 0.0001D)), wi); // Ray toward light
        boolean visible = !bvh.intersectShadowRay(shadowRay, distance);

        return new LightSample(wi, le, pDir, visible, lightPoint, normalAtLight, distance);
    }

    private Light getRandomLight() {
        if (lights.length == 0)
            return null;
        int index = Math.clamp((int) (Math.random() * lights.length), 0, lights.length - 1);
        return lights[index];
    }

    public ImprovedBVH getBvh() {
        return bvh;
    }

    public Camera getCamera() {
        return camera;
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
        private Ray ray;
        private final int i, j;

        public TraceRayTask(Vector3D pixelColor, int bounces, int sampleDepth, int i, int j) {
            this.pixelColor = pixelColor;
            this.bounces = bounces;
            this.sampleDepth = sampleDepth;
            this.i = i;
            this.j = j;
            this.samples = 0;
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
                    blue).getRGB());
            if (renderProgress < (int) progress) {
                renderProgress = (int) progress;
                progressListener.progressUpdated(renderProgress, image);
            }
            lock.unlock();
        }

        private void takeSamples() {
            // First sample at pixel center for base quality
            camera.shootRay(ray, i, j, 0.5D, 0.5D);
            trace(ray, bounces, pixelColor, new Vector3D(1.0), false);
            samples = 1;

            if (sampleDepth == 1)
                return;

            Vector3D tempColor = new Vector3D();
            for (int s = 0; s < sampleDepth; s++) {
                tempColor.set(0, 0, 0);

                double x = Util.randomRange(0, 1.0D);
                double y = Util.randomRange(0, 1.0D);
                camera.shootRay(ray, i, j, x, y);
                trace(ray, bounces, tempColor, new Vector3D(1.0), false);
                pixelColor.add(tempColor);
                samples++;
            }

            // Set final pixel color
            pixelColor.set(Vector3D.div(pixelColor, samples));
        }

        /**
         * Recursive function that traces the path of a primary ray and returns the
         * color of a given ray
         * 
         * @param ray       current ray
         * @param bounce    current bounce
         * @param color     pointer to the color to be calculated by the function
         * @param beta      throughput of the path
         * @param prevDelta whether the previous bounce was a delta bounce
         */
        private void trace(Ray ray, int bounce, Vector3D color, Vector3D beta, boolean prevDelta) {
            // Return if no more bounces
            if (bounce <= 0)
                return;
            // Find intersections using ImprovedBVH
            Hit bvhHit = bvh.intersect(ray, 0.00001D, Double.MAX_VALUE);
            Vector3D pointHit = bvhHit != null ? bvhHit.point() : null;
            Object objectHit = bvhHit != null ? bvhHit.object() : null;
            Vector3D normalHit = bvhHit != null ? bvhHit.normal() : null;
            double minLightDist = Double.MAX_VALUE;
            Vector3D minLightColor = null;
            double minLightBrightness = Double.MAX_VALUE;
            for (Light light : lights) {
                double lightDist = light.intersect(ray);
                if (lightDist > 0.0D && lightDist < minLightDist) {
                    minLightDist = lightDist;
                    minLightColor = light.getColor();
                    minLightBrightness = light.getBrightness();
                }
            }
            // Check if light is hit before an object
            if (objectHit != null && minLightDist < bvhHit.t()) {
                // If light is hit return the color of the light
                double brightness = minLightBrightness;
                color.add(Vector3D.mult(minLightColor, brightness).mult(beta));
                return;
            }
            // If a light is hit, but no object is hit
            if (objectHit == null && minLightColor != null) {
                double brightness = minLightBrightness;
                color.add(Vector3D.mult(minLightColor, brightness).mult(beta));
                return;
            }
            if (objectHit == null) {
                // TODO environment
                return;
            }
            Material material = objectHit.getMaterial();
            Vector3D emittance = material.getEmittance();
            Vector3D shaderNormal = new Vector3D(normalHit);
            Vector3D tangent;
            Vector3D bitangent;
            if (bvhHit.triangle() != null) {
                tangent = bvhHit.triangle().getTangent();
                bitangent = bvhHit.triangle().getBitangent(normalHit);
            } else {
                tangent = objectHit.calcTangent(normalHit);
                bitangent = objectHit.calcBitangent(normalHit, tangent);
            }
            // apply bump map if exists
            if (material.getBumpMap() != null) {
                shaderNormal.set(material.getBumpMap().apply(normalHit, tangent, bitangent, bvhHit.texCoord()));
            }

            // Transform all vectors to world space
            normalHit.set(MathUtils.transformNormalToWS(normalHit, objectHit.getNormalMatrix()));
            shaderNormal.set(MathUtils.transformNormalToWS(shaderNormal, objectHit.getNormalMatrix()));
            tangent = MathUtils.transformDirectionToWS(tangent, objectHit.getTransformMatrix());
            bitangent = MathUtils.transformDirectionToWS(bitangent, objectHit.getTransformMatrix());

            Vector3D wo = new Vector3D(ray.direction()).negated();

            // Ensure a consistent shading frame: normals & basis face wo
            if (normalHit.dot(wo) < 0.0D) {
                normalHit.negate();
                shaderNormal.negate();
                tangent.negate();
                bitangent.negate();
            }

            // Check emittance using transformed normal
            if (emittance.dot(emittance) > 0.0D && normalHit.dot(wo) > 0.0D) {
                if (bounce == bounces || prevDelta) {
                    color.add(Vector3D.mult(emittance, beta));
                }
            }

            if (!prevDelta) {
                LightSample lightSample = sampleLight(pointHit);
                if (lightSample != null && lightSample.visible()) {
                    double cosX = Math.max(0.0D, shaderNormal.dot(lightSample.wi()));
                    if (cosX > 0.0D) {
                        double NgDotV = Math.max(1e-6, normalHit.dot(wo));
                        double NgDotL = Math.max(1e-6, normalHit.dot(lightSample.wi()));
                        double NsDotV = Math.max(1e-6, shaderNormal.dot(wo));
                        double NsDotL = Math.max(1e-6, shaderNormal.dot(lightSample.wi()));

                        double corr = (NgDotV * NgDotL) / Math.max(1e-6, NsDotV * NsDotL);
                        // Clamp to avoid overcompensation
                        corr = Math.min(corr, 4.0);
                        Vector3D f = BSDF.eval(
                                material,
                                material.getAlbedo(bvhHit.texCoord()),
                                wo,
                                lightSample.wi(),
                                material.isThin(),
                                shaderNormal, tangent, bitangent);
                        double pBSDF = BSDF.pdf(
                                material,
                                material.getAlbedo(bvhHit.texCoord()),
                                wo,
                                lightSample.wi(),
                                material.isThin(),
                                shaderNormal, tangent, bitangent);
                        f.mult(corr);
                        pBSDF *= corr;
                        double w = powerHeuristic(lightSample.pDir(), pBSDF);
                        // NEE contribution
                        Vector3D contrib = Vector3D.mult(f, beta)
                                .mult(cosX)
                                .mult(lightSample.le())
                                .div(Math.max(1e-9, lightSample.pDir()))
                                .mult(w);
                        color.add(contrib);
                    }
                }
            }

            // Sample BSDF to continue the path
            BSDFSample s = BSDF.sample(
                    ray,
                    material,
                    shaderNormal,
                    bvhHit.texCoord(),
                    tangent,
                    bitangent);

            // If BSDF sample hits a light, add That direct term with MIS
            for (Light light : lights) {
                Ray newRay = new Ray(Vector3D.add(pointHit, Vector3D.mult(s.l(), 0.000001D)), s.l());
                double lightDist = light.intersect(newRay);
                if (lightDist >= 0.0D) {
                    if (!bvh.intersectShadowRay(newRay, lightDist)) {
                        // Add direct lighting contribution
                        Vector3D pointOnLight = newRay.at(lightDist);
                        Vector3D lightNormal = light.normalAt(pointOnLight);
                        double cosY = Math.max(0.0D, lightNormal.dot(s.l().negated()));
                        double pSelect = 1.0D / lights.length;
                        double pDir = pSelect * (1.0D / Math.max(1e-9, light.getArea())) * (lightDist * lightDist)
                                / Math.max(1e-9, cosY);
                        double w = s.delta() ? 1.0D : powerHeuristic(s.pdf(), pDir);
                        Vector3D le = Vector3D.mult(light.getColor(), light.getBrightness());
                        Vector3D contrib = Vector3D.mult(s.f(), beta)
                                .mult(Math.abs(shaderNormal.dot(s.l())))
                                .div(Math.max(1e-9, s.pdf()))
                                .mult(le)
                                .mult(w);
                        color.add(contrib);
                    }
                }
            }

            beta.mult(s.f())
                    .mult(Math.abs(shaderNormal.dot(s.l())))
                    .div(Math.max(1e-9, s.pdf()));

            // Use proper ray offset direction based on hemisphere
            Vector3D offsetNormal = normalHit.dot(s.l()) > 0 ? normalHit : normalHit.negated();
            Ray newRay = new Ray(Vector3D.add(pointHit, Vector3D.mult(offsetNormal, 0.000001D)), s.l());
            prevDelta = s.delta();

            // Russian roulette
            if (bounce <= bounces - rrStart) {
                double q = Math.clamp(
                        1.0D - Math.clamp((beta.x * 0.212671 + beta.y * 0.715160 + beta.z * 0.072169), 0.0D, 1.0D),
                        0.05D, 0.95D);
                if (MathUtils.random() < q) {
                    return;
                }
                beta.div(1.0D - q);
            }

            trace(newRay, bounce - 1, color, beta, prevDelta);
        }

        private static double powerHeuristic(double pdfA, double pdfB) {
            double a = pdfA * pdfA;
            double b = pdfB * pdfB;
            double s = a + b;
            return (s > 0.0D) ? a / s : 0.0D;
        }
        // private static void shadowPhong(Vector3D shadowPhong, Ray ray, Object
        // objectHit, Ray shadowRay, Vector3D pointHit, Vector3D normalHit, Light light,
        // double lightDist, Vector2D texCoord) {
        // if (lightDist <= 0) return; // Safety check

        // Ray reflectedRay = objectHit.getMaterial().reflectRay(shadowRay, pointHit,
        // normalHit);
        // double kl = Math.max(0D, normalHit.dot(shadowRay.direction().normalized())) *
        // objectHit.getMaterial().getLambertian();
        // double ks = Math.pow(Math.max(0,
        // ray.direction().normalized().dot(reflectedRay.direction().normalized())),
        // objectHit.getMaterial().getSpecularExponent()) *
        // objectHit.getMaterial().getSpecular();
        // Vector3D s = Vector3D.mult(objectHit.getMaterial().getColor(texCoord),
        // objectHit.getMaterial().getMetalness()).add(new Vector3D(1, 1, 1).mult(1 -
        // objectHit.getMaterial().getMetalness()));
        // Vector3D diffuse = new
        // Vector3D(objectHit.getMaterial().getColor(texCoord)).mult(light.getColor()).mult(kl);
        // Vector3D specular = new Vector3D(light.getColor()).mult(s).mult(ks);
        // double brightness = light.getBrightness() / lightDist;
        // diffuse.mult(brightness);
        // specular.mult(brightness);
        // shadowPhong.add(Vector3D.add(diffuse, specular));
        // }
    }
}
