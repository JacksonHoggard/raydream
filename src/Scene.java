import light.Light;
import material.Material;
import math.Ray;
import math.Vector3D;
import math.Vector4D;
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
    }

    public void render(String filename, int samples, int bounces, int threads) throws IOException {
        long startTime = System.nanoTime();

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for(int j = 0; j < height; j++) {
            for(int i = 0; i < width; i++) {
                pool.execute(new TraceRayTask(image, bounces, samples, i, j));
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
        System.out.println("Finished in " + durationSeconds + "s");
    }

    class TraceRayTask implements Runnable {
        private final BufferedImage image;
        private final int bounces;
        private final int samples;
        private final int i, j;

        public TraceRayTask(BufferedImage image, int bounces, int samples, int i, int j) {
            this.image = image;
            this.bounces = bounces;
            this.samples = samples;
            this.i = i;
            this.j = j;
        }

        public void run() {
            Vector3D totalColor = new Vector3D(0, 0, 0);
            for(int s = 0; s < samples; s++) {
                Ray ray = camera.shootRay(i, j);
                totalColor.add(trace(ray, bounces));
            }
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
            Vector3D pointHit = null;
            Object objectHit = null;
            Vector3D normalHit = null;
            double minDist = Double.MAX_VALUE;
            for(Object object : objects) {
                Vector4D rOriginOS = new Vector4D(ray.getOrigin().x, ray.getOrigin().y, ray.getOrigin().z, 1);
                Vector4D rDirOS = new Vector4D(ray.getDirection().x, ray.getDirection().y, ray.getDirection().z, 0);
                rOriginOS = rOriginOS.mult(object.getInverseTransformMatrix());
                rDirOS = rDirOS.mult(object.getInverseTransformMatrix());
                Ray rayOS = new Ray(new Vector3D(rOriginOS.x, rOriginOS.y, rOriginOS.z), new Vector3D(rDirOS.x, rDirOS.y, rDirOS.z));
                Hit hit = object.intersect(rayOS);
                double t = hit.t();
                if(t > 0 && t < minDist) {
                    objectHit = object;
                    minDist = t;
                    pointHit = ray.at(t);
                    normalHit = hit.normal();
                }
            }
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
                for(Object object : objects) {
                    Vector4D shadowRayOriginOS = new Vector4D(shadowRay.getOrigin().x, shadowRay.getOrigin().y, shadowRay.getOrigin().z, 1);
                    Vector4D shadowRayDirOS = new Vector4D(shadowRay.getDirection().x, shadowRay.getDirection().y, shadowRay.getDirection().z, 0);
                    shadowRayOriginOS = shadowRayOriginOS.mult(object.getInverseTransformMatrix());
                    shadowRayDirOS = shadowRayDirOS.mult(object.getInverseTransformMatrix());
                    Ray shadowRayOS = new Ray(
                            new Vector3D(shadowRayOriginOS.x, shadowRayOriginOS.y, shadowRayOriginOS.z),
                            new Vector3D(shadowRayDirOS.x, shadowRayDirOS.y, shadowRayDirOS.z)
                    );
                    Hit hit = object.intersect(shadowRayOS);
                    double t = hit.t();
                    if(t > 0 && t < lightDist) {
                        inShadow = true;
                        break;
                    }
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
