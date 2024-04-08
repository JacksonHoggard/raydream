import light.Light;
import material.Glass;
import material.Material;
import material.Mirror;
import math.Ray;
import math.Vector3D;
import material.Matte;
import object.Box;
import object.Object;
import object.Sphere;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    // Image
    static int width = 1920;
    static int height = 1080;

    // Ray data
    static int bounces = 128;
    static int samples = 100;

    // Camera
    static Vector3D origin = new Vector3D(0, 0, 0);
    static double focalLength = 1.0D;
    static double vHeight = 2.0D;
    static double vWidth = vHeight * (width / (double) height);

    // Lights
    static final Vector3D AMBIENT = new Vector3D(1, 1, 1);
    static final Light[] LIGHTS = {
            new Light(new Vector3D(1, 1, -0.5D), new Vector3D(1, 1, 1), 2),
            new Light(new Vector3D(1, 2, -1), new Vector3D(0.99D, 0.01D, 0.01D), 1)
    };

    static final Object[] OBJECTS = new Object[] {
            //new Sphere(new Vector3D(0, 0, -2), 0.5D, new Mirror(new Vector3D(224 / 255D, 74 / 255D, 89 / 255D))),
            new Sphere(new Vector3D(0, 0, -2), -0.5D, new Glass(new Vector3D(1, 1, 1), 0.15D, 0.3D, 0.9D, 32, 1.06D)),
            new Sphere(new Vector3D(-0.5, 0.2, -3), 0.5D, new Matte(new Vector3D(0.9, 0.1, 0.9), 0.1D, 0.6D, 0.5D, 4)),
            new Box(new Vector3D(-0.62, -0.5, -1), new Vector3D(-0.9, 0.3, -2), new Matte(new Vector3D(88 / 255D, 124 / 255D, 166 / 255D), 0.1D, 0.6D, 0.5D, 4)),
            //new Box(new Vector3D(-0.62, -0.5, -1), new Vector3D(-0.9, 0.3, -2), new Mirror(new Vector3D(1D, 1D, 1D))),
            //new Sphere(new Vector3D(0, -100.5, -2), 100D, new Matte(new Vector3D(184 / 255D, 166 / 255D, 97 / 255D), 0.2D, 0.6D, 0.5D, 1)),
            new Sphere(new Vector3D(0, -100.5, -2), 100D, new Mirror(new Vector3D(184 / 255D, 166 / 255D, 97 / 255D))),
    };

    public static void main(String[] args) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for(int j = 0; j < height; j++) {
            System.out.println("Progress: " + (int)((j / (double)(height - 1)) * 100) + "% (" + (j + 1) + "/" + height + ")");
            for(int i = 0; i < width; i++) {
                Vector3D totalColor = new Vector3D(0, 0, 0);
                for(int s = 0; s < samples; s++) {
                    // Calculate the direction of the ray
                    Ray ray = new Ray(origin, new Vector3D());
                    shootRay(i, j, ray);
                    totalColor.add(trace(ray, bounces));
                }
                totalColor.div(samples);
                //System.out.println(totalColor);
                image.setRGB(i, j, new Color((int) Math.min(totalColor.x * 255, 255), (int) Math.min(totalColor.y * 255, 255), (int) Math.min(totalColor.z * 255, 255)).getRGB());
            }
        }

        File output = new File("output.png");
        ImageIO.write(image, "png", output);
    }

    /**
     * Recursive function that traces the path of a primary ray and returns the color of a given ray
     * @param ray current ray
     * @param bounce current bounce
     */
    private static Vector3D trace(Ray ray, int bounce) {
        // Return if no more bounces
        if(bounce <= 0)
            return new Vector3D(0, 0, 0);
        // Find intersections
        Vector3D pointHit = new Vector3D();
        Object objectHit = null;
        Vector3D normalHit = null;
        double minDist = Double.MAX_VALUE;
        for(Object object : OBJECTS) {
            double t = object.intersect(ray);
            if(t > 0) {
                double distance = origin.distance(pointHit);
                if(distance < minDist) {
                    objectHit = object;
                    minDist = distance;
                    pointHit = ray.at(t);
                    normalHit = objectHit.normalAt(pointHit);
                }
            }
        }
        // Return sky if no object is hit
        if(objectHit == null) {
            return new Vector3D(0.125, 0.125, 0.125);
        }
        Material material = objectHit.getMaterial();
        Vector3D reflectionColor = new Vector3D();
        Vector3D refractionColor = new Vector3D();
        switch(material.getType()) {
            case REFLECT -> {
                reflectionColor.set(trace(material.reflectRay(ray, pointHit, normalHit), bounce - 1));
                return phong(ray, objectHit, pointHit, normalHit).add(reflectionColor);
            }
            case REFLECT_REFRACT -> {
                double kr = material.fresnel(ray, normalHit);
                Ray reflectionRay = material.reflectRay(ray, pointHit, normalHit);
                Ray refractionRay = material.refractRay(ray, pointHit, normalHit);
                reflectionColor.set(trace(reflectionRay, bounce - 1));
                refractionColor.set(trace(refractionRay, bounce - 1));
                return phong(ray, objectHit, pointHit, normalHit).add(reflectionColor.mult(kr).add(refractionColor.mult(1 - kr)));
            }
        }
        return phong(ray, objectHit, pointHit, normalHit);
    }

    private static Vector3D phong(Ray ray, Object objectHit, Vector3D pointHit, Vector3D normalHit) {
        Vector3D diffuse = new Vector3D();
        Vector3D specular = new Vector3D();
        Vector3D color = Vector3D.mult(objectHit.getMaterial().getAlbedo(), objectHit.getMaterial().getAmbient()).mult(AMBIENT);
        for(Light light : LIGHTS) {
            Vector3D shadowDir = Vector3D.sub(light.position(), pointHit).normalize();
            Ray shadowRay = new Ray(Vector3D.add(pointHit, Vector3D.mult(shadowDir, 0.00001D)), shadowDir);
            boolean inShadow = false;
            double lightDist = light.intersect(shadowRay);
            for(Object object : OBJECTS) {
                double t = object.intersect(shadowRay);
                if(t > 0 && t < lightDist) {
                    inShadow = true;
                    break;
                }
            }
            if(!inShadow) {
                Ray reflectedRay = objectHit.getMaterial().reflectRay(shadowRay, pointHit, normalHit);
                double kl = Math.max(0D, normalHit.dot(shadowRay.getDirection())) * objectHit.getMaterial().getLambertian();
                double ks = Math.pow(Math.max(0, ray.getDirection().dot(reflectedRay.getDirection())), objectHit.getMaterial().getSpecularExponent()) * objectHit.getMaterial().getSpecular();
                Vector3D s = Vector3D.mult(objectHit.getMaterial().getAlbedo(), objectHit.getMaterial().getMetalness()).add(new Vector3D(1, 1, 1).mult(1 - objectHit.getMaterial().getMetalness()));
                s.mult(ks);
                diffuse.set(objectHit.getMaterial().getAlbedo()).mult(kl);
                specular.set(light.color()).mult(s);
                double brightness = light.brightness() / (light.brightness() + lightDist);
                color.add(Vector3D.add(diffuse, specular).mult(brightness));
            }
        }
        return color;
    }

    /**
     * Finds the direction of the primary ray based on the pixel coordinates
     * @param i pixel row index
     * @param j pixel column index
     * @param ray primary ray
     */
    private static void shootRay(int i, int j, Ray ray) {
        double u = (i / (double) (width - 1)) * vWidth;
        double v = (j / (double) (height - 1)) * vHeight;
        double pixelSizeX = vWidth / width;
        double pixelSizeY = vHeight / height;
        double pixelXVariation = randomRange(-pixelSizeX/2, pixelSizeX/2);
        double pixelYVariation = randomRange(-pixelSizeY/2, pixelSizeY/2);
        ray.getDirection().z -= focalLength;
        ray.getDirection().x -= (0.5D * vWidth);
        ray.getDirection().y += (0.5D * vHeight);
        ray.getDirection().x += u + pixelXVariation;
        ray.getDirection().y -= v - pixelYVariation;
    }

    private static double randomRange(double min, double max) {
        return min + (max - min) * Math.random();
    }
}