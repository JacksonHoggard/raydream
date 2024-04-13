import light.Light;
import material.*;
import material.texture.Checker;
import math.Vector3D;
import object.Box;
import object.Object;
import object.Plane;
import object.Sphere;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        int width = 1920;
        int height = 1080;

        Camera camera = new Camera(
                new Vector3D(-2, 1, 1),
                new Vector3D(0, 0, -2),
                50,
                width,
                height
        );

        Light ambient = new Light(new Vector3D(), new Vector3D(1, 1, 1), 1);
        Light[] lights = {
                new Light(new Vector3D(-10, 10, -10), new Vector3D(1, 1, 1), 10D),
                new Light(new Vector3D(-10, 10, 10), new Vector3D(1, 1, 1), 10D),
                new Light(new Vector3D(10, 10, -10), new Vector3D(1, 1, 1), 10D),
                new Light(new Vector3D(10, 10, 10), new Vector3D(1, 1, 1), 10D)
        };

        Object[] objects = new Object[] {
                new Sphere(new Vector3D(0, 0, -2), 0.5D, new Glass(0.1D, 0.94)),
                new Sphere(new Vector3D(-0.5, 0, -3), 0.5D, new Reflective(new Vector3D(1, 0, 0), 0.1D, 0.6D, 0.6D, 50, 0.2D, 0.617D, 2.63D)),
                new Sphere(new Vector3D(0, -0.25, -1), 0.25D, new Reflective(new Vector3D(0, 1, 0), 0.1D, 0.6D, 0.6D, 50, 0.2D, 0.617D, 2.63D)),
                new Box(new Vector3D(1, -0.5, -1.5), new Vector3D(2, 0.5, -2.5), new Reflective(new Vector3D(0D, 0D, 1D), 0.1D, 0.4D, 0.5, 32, 0.3D, 0.177, 3.638)),
                new Box(new Vector3D(-1.5, -0.5, -1), new Vector3D(-1, 0, -1.5),  new Pattern(new Checker(2, 2, new Vector3D(0, 0, 0), new Vector3D(1, 1, 1)), 0.1D, 0.6D, 0.6D, 50, 0.6D, 2.485D, 3.433D)),
                new Plane(-0.5, new Vector3D(0, 1, 0), new Reflective(new Vector3D(255/255D, 253/255D, 208/255D), 0.1D, 0.6D, 0.5D, 4, 1, 0.617D, 2.63D))
        };

        Scene scene = new Scene(camera, ambient, lights, objects, width, height);
        scene.render("output.png", 100, 128);
    }
}