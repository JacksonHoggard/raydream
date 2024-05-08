import light.Light;
import material.Material;
import math.Ray;
import math.Vector2D;
import math.Vector3D;
import object.BVH;
import object.Hit;
import object.Object;

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
    private final BVH bvh;
    private final int width;
    private final int height;
    private static int threadCounter;
    private static final Lock lock = new ReentrantLock();

    public Scene(Camera camera, Light ambient, Light[] lights, Object[] objects, int width, int height) {
        this.camera = camera;
        this.ambient = ambient;
        this.lights = lights;
        this.objects = objects;
        this.width = width;
        this.height = height;
        threadCounter = width * height;
        bvh = new BVH(objects);
    }

    public void render(String filename, int sampleDepth, int bounces, int threads) throws IOException {
        long startTime = System.nanoTime();

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for(int j = 0; j < height; j++) {
            for(int i = 0; i < width; i++) {
                pool.execute(new TraceRayTask(image, bounces, sampleDepth, i, j));
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
            System.out.println("Finished in " + minutes + "m " + (durationSeconds - (minutes * 60)) + "s");
        else
            System.out.println("Finished in " + durationSeconds + "s");
    }

    private class TraceRayTask implements Runnable {
        private final BufferedImage image;
        private final int bounces;
        private final int sampleDepth;
        private int samples;
        private int reusedRayIdx;
        private Vector3D reusedColor;
        private final int i, j;

        public TraceRayTask(BufferedImage image, int bounces, int sampleDepth, int i, int j) {
            this.image = image;
            this.bounces = bounces;
            this.sampleDepth = sampleDepth;
            this.i = i;
            this.j = j;
            this.samples = 0;
            this.reusedRayIdx = -1;
        }

        public void run() {
            Vector3D totalColor = new Vector3D(0, 0, 0);
            totalColor.add(takeSamples(sampleDepth, 0, 0, 1, -1));
            totalColor.div(samples);
            image.setRGB(i, j, new Color((int) Math.min(totalColor.x * 255, 255), (int) Math.min(totalColor.y * 255, 255), (int) Math.min(totalColor.z * 255, 255)).getRGB());
            printProgress();
        }

        private void printProgress() {
            lock.lock();
            threadCounter--;
            double progress = (((((width * height) - threadCounter) / (double) (width * height))) * 100);
            if(progress % 1 == 0)
                System.out.println("Progress: " + (long) progress + "% (" + ((width * height) - threadCounter) + "/" + (width * height) + ")");
            lock.unlock();
        }

        private Vector3D takeSamples(int sampleDepth, double tlx, double tly, double brx, double bry) {
            // Return black if maximum sample depth reached
            if(sampleDepth <= 0)
                return new Vector3D(0, 0, 0);
            double w = brx - tlx;
            double h = tly - bry;
            Ray rayTL = reusedRayIdx != 0 ? camera.shootRay(i, j, tlx, tly) : null;
            Ray rayTR = reusedRayIdx != 1 ? camera.shootRay(i, j, tlx + w, bry + h) : null;
            Ray rayBL = reusedRayIdx != 2 ? camera.shootRay(i, j, tlx, bry) : null;
            Ray rayBR = reusedRayIdx != 3 ? camera.shootRay(i, j, brx, bry) : null;
            Vector3D colorTL = reusedRayIdx != 0 ? trace(rayTL, bounces) : reusedColor;
            Vector3D colorTR = reusedRayIdx != 1 ? trace(rayTR, bounces) : reusedColor;
            Vector3D colorBL = reusedRayIdx != 2 ? trace(rayBL, bounces) : reusedColor;
            Vector3D colorBR = reusedRayIdx != 3 ? trace(rayBR, bounces) : reusedColor;
            Vector3D totalColor = Vector3D.add(colorTL, colorTR).add(colorBL).add(colorBR).div(4);
            Vector3D temp = new Vector3D(0, 0, 0);
            samples++;
            if(colorTL.distance(totalColor) > 0.01) {
                reusedColor = colorTL;
                reusedRayIdx = 0;
                temp.add(takeSamples(sampleDepth - 1, tlx, tly, brx - (w / 2), bry + (h / 2)));
            }
            if(colorTR.distance(totalColor) > 0.01) {
                reusedColor = colorTR;
                reusedRayIdx = 1;
                temp.add(takeSamples(sampleDepth - 1, tlx + (w / 2), tly, brx, bry + (h / 2)));
            }
            if(colorBL.distance(totalColor) > 0.01) {
                reusedColor = colorBL;
                reusedRayIdx = 2;
                temp.add(takeSamples(sampleDepth - 1, tlx, tly - (h / 2), brx - (w / 2), bry));
            }
            if(colorBR.distance(totalColor) > 0.01) {
                reusedColor = colorBL;
                reusedRayIdx = 3;
                temp.add(takeSamples(sampleDepth - 1, tlx + (w / 2), tly - (h / 2), brx, bry));
            }
            return totalColor.add(temp);
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
            // Return sky if no object is hit
            if(objectHit == null) {
                return new Vector3D(0.125, 0.125, 0.125);
            }
            Vector3D reflectionNormal = ray.getDirection().normalized().dot(normalHit) < 0 ?
                    new Vector3D(normalHit).negate() : normalHit;
            Vector3D refractionNormal = ray.getDirection().normalized().dot(normalHit) < 0 ?
                    normalHit : new Vector3D(normalHit).negate();
            Material material = objectHit.getMaterial();
            Vector3D reflectionColor = new Vector3D();
            Vector3D refractionColor = new Vector3D();
            switch(material.getType()) {
                case REFLECT -> {
                    double kr = material.fresnelMetal(ray, normalHit);
                    reflectionColor.set(trace(material.reflectRay(ray, pointHit, reflectionNormal), bounce - 1));
                    if(!material.hasColor())
                        objectHit.getMaterial().getColor(objectHit, pointHit).set(Vector3D.mult(reflectionColor, kr));
                    return phong(ray, objectHit, pointHit, normalHit).add(Vector3D.mult(reflectionColor, kr));
                }
                case REFLECT_REFRACT -> {
                    double kr = material.fresnelDielectric(ray, normalHit);
                    Ray reflectionRay = material.reflectRay(ray, pointHit, reflectionNormal);
                    Ray refractionRay = material.refractRay(ray, pointHit, refractionNormal);
                    reflectionColor.set(trace(reflectionRay, bounce - 1));
                    refractionColor.set(trace(refractionRay, bounce - 1));
                    if(!material.hasColor()) objectHit.getMaterial().getColor(objectHit, pointHit).set(Vector3D.mult(reflectionColor, kr).add(Vector3D.mult(refractionColor, 1 - kr)));
                    return phong(ray, objectHit, pointHit, normalHit).add(Vector3D.mult(reflectionColor, kr).add(Vector3D.mult(refractionColor, 1 - kr)));
                }
            }
            return phong(ray, objectHit, pointHit, normalHit);
        }

        private Vector3D phong(Ray ray, Object objectHit, Vector3D pointHit, Vector3D normalHit) {
            Vector3D diffuse = new Vector3D();
            Vector3D specular = new Vector3D();
            Vector3D color = Vector3D.mult(objectHit.getMaterial().getColor(objectHit, pointHit), objectHit.getMaterial().getAmbient()).mult(ambient.color());
            for(Light light : lights) {
                Vector3D shadowDir = Vector3D.sub(light.position(), pointHit).normalize();
                Ray shadowRay = new Ray(Vector3D.add(pointHit, Vector3D.mult(shadowDir, 0.00001D)), shadowDir);
                boolean inShadow = false;
                double lightDist = light.intersect(shadowRay);
                Hit bvhHit = bvh.intersect(shadowRay, objects);
                if(bvhHit.object() != null) {
                    if(bvhHit.t() > 0 && bvhHit.t() < lightDist)
                        inShadow = true;
                }
                if(!inShadow) {
                    Ray reflectedRay = objectHit.getMaterial().reflectRay(shadowRay, pointHit, normalHit);
                    double kl = Math.max(0D, normalHit.dot(shadowRay.getDirection().normalized())) * objectHit.getMaterial().getLambertian();
                    double ks = Math.pow(Math.max(0, ray.getDirection().normalized().dot(reflectedRay.getDirection().normalized())), objectHit.getMaterial().getSpecularExponent()) * objectHit.getMaterial().getSpecular();
                    Vector3D s = Vector3D.mult(objectHit.getMaterial().getColor(objectHit, pointHit), objectHit.getMaterial().getMetalness()).add(new Vector3D(1, 1, 1).mult(1 - objectHit.getMaterial().getMetalness()));
                    diffuse.set(objectHit.getMaterial().getColor(objectHit, pointHit)).mult(kl);
                    specular.set(light.color()).mult(s).mult(ks);
                    double brightness = light.brightness() / (light.brightness() + lightDist);
                    color.add(Vector3D.add(diffuse, specular).mult(brightness));
                }
            }
            return color;
        }
    }

}
