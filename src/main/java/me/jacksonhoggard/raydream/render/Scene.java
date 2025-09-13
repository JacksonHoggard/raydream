package me.jacksonhoggard.raydream.render;

import me.jacksonhoggard.raydream.config.ApplicationConfig;
import me.jacksonhoggard.raydream.core.ApplicationContext;
import me.jacksonhoggard.raydream.light.Light;
import me.jacksonhoggard.raydream.light.LightSample;
import me.jacksonhoggard.raydream.material.*;
import me.jacksonhoggard.raydream.material.bxdf.BxDF;
import me.jacksonhoggard.raydream.material.bxdf.BxDF.BxDFSample;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.*;
import me.jacksonhoggard.raydream.object.Object;
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
    private final BVH bvh;
    private final BufferedImage image;
    private final int width;
    private final int height;
    private final double lightWeightSum;
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
        this.lightWeightSum = Arrays.stream(lights).mapToDouble(l -> l.samplingWeight()).sum();
        this.threadCounter = width * height;
        this.renderProgress = 0;
        ArrayList<Primitive> prims = new ArrayList<>();
        for (Object o : objects)
            Collections.addAll(prims, o);
        for (Light l : lights)
            Collections.addAll(prims, l);
        this.bvh = new BVH(prims);
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

    public LightSample sampleLight(Vector3D p) {
        if (lights.length == 0 || lightWeightSum == 0.0D)
            return null;

        // Discrete selection (power weighted)
        double r = MathUtils.random() * lightWeightSum;
        Light chosen = null;
        double accum = 0.0D;
        for (Light L : lights) {
            double w = Math.max(0.0D, L.samplingWeight());
            accum += w;
            if (r <= accum) {
                chosen = L;
                break;
            }
        }
        if (chosen == null)
            chosen = lights[lights.length - 1];

        double pSelect = Math.max(0.0D, chosen.samplingWeight()) / lightWeightSum;
        if (pSelect == 0.0D)
            return null;

        // Sample the chosen light conditionally
        LightSample s = chosen.sampleLight(p);
        if (s == null)
            return null;

        // Combine PDFs
        double pdfLight = chosen.isDelta() ? pSelect : (pSelect * s.pdf());
        if (pdfLight <= 0.0D)
            return null;

        return new LightSample(s.wi(), s.Li(), s.dist(), pdfLight, chosen.isDelta());
    }

    public BVH getBvh() {
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
            pixelColor.add(trace(ray));
            samples = 1;

            if (sampleDepth == 1)
                return;

            for (int s = 0; s < sampleDepth; s++) {
                double x = Util.randomRange(0, 1.0D);
                double y = Util.randomRange(0, 1.0D);
                camera.shootRay(ray, i, j, x, y);
                pixelColor.add(trace(ray));
                samples++;
            }

            // Set final pixel color
            pixelColor.div(samples);
        }

        private Vector3D trace(Ray ray) {
            final double EPS = 1e-5D;

            Vector3D L = new Vector3D(0, 0, 0); // Accumulated radiance
            Vector3D beta = new Vector3D(1, 1, 1); // Path throughput
            boolean prevDelta = true; // Whether the previous bounce was a delta bounce (for light hits)

            for (int bounce = 0; bounce < bounces; bounce++) {
                // Intersect scene
                Hit hit = bvh.intersect(ray, EPS, Double.MAX_VALUE);

                // Miss -> Accumulate sky color and terminate
                if (hit == null) {
                    if (skyColor != null && skyColor.dot(skyColor) > 0.0D) {
                        L.add(Vector3D.mult(beta, skyColor));
                    }
                    break;
                }

                // Shading context
                Vector3D p = hit.point();
                Vector3D ng = new Vector3D(hit.normal());
                Vector3D ns = new Vector3D(hit.normal());
                Vector3D wo = ray.direction().negated();
                Material<? extends BxDF> mat = !hit.primitive().isLight() ? ((Object) hit.primitive()).getMaterial() : null;
                Vector2D uv = hit.texCoord();
                Vector3D tan = new Vector3D();
                Vector3D bitan = new Vector3D();
                if (hit.triangle() != null) {
                    tan = hit.triangle().getTangent();
                    bitan = hit.triangle().getBitangent(ns);
                } else if (!hit.primitive().isLight()) {
                    tan = ((Object) hit.primitive()).calcTangent(ns);
                    bitan = ((Object) hit.primitive()).calcBitangent(ns, tan);
                }
                // Apply bump map if exists
                if (!hit.primitive().isLight()) {
                    if (mat.getBumpMap() != null) {
                        ns.set(mat.getBumpMap().apply(ns, tan, bitan, uv));
                    }
                    // Transform all vectors to world space
                    ns.set(MathUtils.transformNormalToWS(ns, ((Object) hit.primitive()).getNormalMatrix())).normalize();
                    ng.set(MathUtils.transformNormalToWS(ng, ((Object) hit.primitive()).getNormalMatrix())).normalize();
                    tan = MathUtils.transformDirectionToWS(tan, ((Object) hit.primitive()).getTransformMatrix()).normalize();
                    bitan = MathUtils.transformDirectionToWS(bitan, ((Object) hit.primitive()).getTransformMatrix()).normalize();
                }

                // Add emission when hitting a light source
                if (hit.primitive().isLight() || mat.isEmissive()) {
                    Vector3D Le = hit.primitive().isLight() ? Vector3D.mult(((Light) hit.primitive()).getColor(), ((Light) hit.primitive()).getBrightness()) : mat.getEmittance();
                    if (bounce == 0 || prevDelta) {
                        L.add(Vector3D.mult(beta, Le));
                    }
                    if (hit.primitive().isLight())
                        break; // Light hit -> terminate
                }

                boolean entering = ng.dot(wo) > 0;

                // Absorption for transmission
                if(!entering) {
                    beta.mult(
                        new Vector3D(
                            Math.exp(-hit.t() * mat.getAlbedo(uv).x),
                            Math.exp(-hit.t() * mat.getAlbedo(uv).y),
                            Math.exp(-hit.t() * mat.getAlbedo(uv).z))
                    );
                }

                BxDF bxdf = mat.createBxDF(ng, ns, uv);

                // Next event estimation (sample lights) with MIS
                // Pick a light, sample a direction wi toward it, shadow test, and accumulate.
                LightSample ls = sampleLight(p);
                if (ls != null && ls.pdf() > 0.0D && !ls.Li().equals(Vector3D.ZERO)) {
                    Ray shadow = new Ray(
                            Vector3D.add(p, Vector3D.mult(ng, EPS)),
                            ls.wi());
                    boolean visible = !bvh.intersectShadowRay(shadow, ls.dist() - EPS);

                    if (visible) {
                        Vector3D f = bxdf.eval(wo, ls.wi());
                        double cos = Math.abs(ns.dot(ls.wi()));
                        double bsdfPdf = bxdf.pdf(wo, ls.wi());
                        double w = powerHeuristic(ls.pdf(), bsdfPdf);
                        if (bsdfPdf > 0.0D) {
                            Vector3D contrib = f.mult(cos * w / ls.pdf());
                            if (!contrib.equals(Vector3D.ZERO)) {
                                L.add(Vector3D.mult(beta, Vector3D.mult(contrib, ls.Li())));
                            }
                        }
                    }
                }

                // Sample BSDF to continue the path
                BxDFSample s = bxdf.sample(wo);
                if (s == null || s.pdf() <= 0.0D || s.f().equals(Vector3D.ZERO)) {
                    break;
                }

                // Throughput update: beta *= f * |n . wi| / pdf
                double cos = Math.abs(ng.dot(s.wi()));
                beta.mult(Vector3D.mult(s.f(), cos).div(s.pdf()));

                if (bounce >= rrStart) {
                    double maxBeta = Math.max(beta.x, Math.max(beta.y, beta.z));
                    maxBeta = Math.min(maxBeta, 0.99D);
                    if (MathUtils.random() > maxBeta) {
                        break;
                    }
                    beta.mult(1.0D / maxBeta);
                }

                // Spawn next ray
                Vector3D origin;
                if(s.event() == BxDF.Event.TRANSMIT) {
                    origin = s.wi().dot(ng) < 0 ?
                        Vector3D.sub(p, Vector3D.mult(ng, EPS)) :
                        Vector3D.add(p, Vector3D.mult(ng, EPS));
                } else {
                    origin = Vector3D.add(p, Vector3D.mult(ng, EPS));
                }
                ray = new Ray(origin, s.wi());
                prevDelta = s.isDelta();
            }

            return L;
        }

        private static double powerHeuristic(double pdfA, double pdfB) {
            double a = pdfA * pdfA;
            double b = pdfB * pdfB;
            double s = a + b;
            return a / s;
        }
    }
}
