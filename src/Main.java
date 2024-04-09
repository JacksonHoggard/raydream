import light.Light;
import material.*;
import math.Ray;
import math.Vector3D;
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
    static Camera camera = new Camera(
            new Vector3D(-2, 1, 1),
            new Vector3D(0, 0, -2),
            50,
            width,
            height
    );

    // Lights
    static final Vector3D AMBIENT = new Vector3D(1, 1, 1);
    static final Light[] LIGHTS = {
            new Light(new Vector3D(-2, 0.5, 0), new Vector3D(1, 1, 1), 1D),
            new Light(new Vector3D(-1, 0.5, 0), new Vector3D(1, 1, 1), 1D),
            new Light(new Vector3D(0, 0.5, 0), new Vector3D(1, 1, 1), 1D),
            new Light(new Vector3D(1, 0.5, 0), new Vector3D(1, 1, 1), 1D),
            new Light(new Vector3D(-2, 0.5, -5), new Vector3D(1, 1, 1), 1D),
            new Light(new Vector3D(-1, 0.5, -5), new Vector3D(1, 1, 1), 1D),
            new Light(new Vector3D(0, 0.5, -5), new Vector3D(1, 1, 1), 1D),
            new Light(new Vector3D(1, 0.5, -5), new Vector3D(1, 1, 1), 1D)
    };

    static final Object[] OBJECTS = new Object[] {
            //new Sphere(new Vector3D(0, 0, -2), 0.5D, new Mirror(new Vector3D(224 / 255D, 74 / 255D, 89 / 255D))),
            //new Sphere(new Vector3D(0, 0, -2), 0.5D, new Matte(new Vector3D(224 / 255D, 74 / 255D, 89 / 255D), 0.1D, 0.6D, 0.3D, 32)),
            new Sphere(new Vector3D(0, 0, -2), 0.5D, new Glass(0.1D, 0.94)),
            //new Sphere(new Vector3D(-0.5, 0.2, -3), 0.5D, new Mirror(new Vector3D(0.9, 0.1, 0.9))),
            new Sphere(new Vector3D(-0.5, 0, -3), 0.5D, new Reflective(new Vector3D(1, 0, 0), 0.1D, 0.6D, 0.3D)),
            //new Box(new Vector3D(-0.62, -0.5, -1), new Vector3D(-0.9, 0.3, -2), new Matte(new Vector3D(88 / 255D, 124 / 255D, 166 / 255D), 0.1D, 0.6D, 0.5D, 4)),
            //new Box(new Vector3D(-0.62, -0.5, -1), new Vector3D(-0.9, 0.3, -2), new Glass(new Vector3D(1D, 1D, 1D), 0.15D, 0.3D, 0.9D, 32, 0.94D)),
            new Box(new Vector3D(1, -0.5, -1.5), new Vector3D(2, 0.5, -2.5), new Reflective(new Vector3D(0D, 0D, 1D), 0.1D, 0.1D, 1)),
            new Box(new Vector3D(-1.5, -0.5, -1), new Vector3D(-1, 0, -1.5), new ColoredGlass(new Vector3D(0, 1, 0), 0.1D, 0.94)),
            //new Sphere(new Vector3D(0, -100.5, -2), 100D, new Matte(new Vector3D(184 / 255D, 166 / 255D, 97 / 255D), 0.2D, 0.6D, 0.5D, 1)),
            new Sphere(new Vector3D(0, -10000.5, -2), 10000D, new Reflective(new Vector3D(255/255D, 253/255D, 208/255D), 0.1D, 0.6D, 0.5D))
    };
    public static void main(String[] args) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for(int j = 0; j < height; j++) {
            System.out.println("Progress: " + (int)((j / (double)(height - 1)) * 100) + "% (" + (j + 1) + "/" + height + ")");
            for(int i = 0; i < width; i++) {
                Vector3D totalColor = new Vector3D(0, 0, 0);
                for(int s = 0; s < samples; s++) {
                    // Calculate the direction of the ray
                    Ray ray = camera.shootRay(i, j);
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
                double distance = ray.getOrigin().distance(ray.at(t));
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
                if(!material.hasColor())
                    objectHit.getMaterial().getAlbedo().set(reflectionColor);
                return phong(ray, objectHit, pointHit, normalHit).add(reflectionColor);
            }
            case REFLECT_REFRACT -> {
                double kr = material.fresnel(ray, normalHit);
                Ray reflectionRay = material.reflectRay(ray, pointHit, normalHit);
                Ray refractionRay = material.refractRay(ray, pointHit, normalHit);
                reflectionColor.set(trace(reflectionRay, bounce - 1));
                refractionColor.set(trace(refractionRay, bounce - 1));
                if(!material.hasColor()) objectHit.getMaterial().getAlbedo().set(reflectionColor);
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
                double kl = Math.max(0D, normalHit.dot(shadowRay.getDirection().normalized())) * objectHit.getMaterial().getLambertian();
                double ks = Math.pow(Math.max(0, ray.getDirection().normalized().dot(reflectedRay.getDirection().normalized())), objectHit.getMaterial().getSpecularExponent()) * objectHit.getMaterial().getSpecular();
                Vector3D s = Vector3D.mult(objectHit.getMaterial().getAlbedo(), objectHit.getMaterial().getMetalness()).add(new Vector3D(1, 1, 1).mult(1 - objectHit.getMaterial().getMetalness()));
                diffuse.set(objectHit.getMaterial().getAlbedo()).mult(kl);
                specular.set(light.color()).mult(s).mult(ks);
                double brightness = light.brightness() / (light.brightness() + lightDist);
                color.add(Vector3D.add(diffuse, specular).mult(brightness));
            }
        }
        return color;
    }
}