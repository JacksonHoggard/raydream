import light.Light;
import material.*;
import material.texture.Checker;
import math.Vector3D;
import object.*;
import object.Object;
import util.Util;

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
                new Sphere(new Transform(new Vector3D(0, 0, -2), new Vector3D(0, 0, 0), new Vector3D(1, 1, 1)), 0.5D, new Glass(0.1D, 0.94)),
                new Sphere(new Transform(new Vector3D(-0.5, 0, -3), new Vector3D(0, 0, 0), new Vector3D(1, 1, 1)), 0.5D, new Reflective(new Vector3D(1, 0, 0), 0.1D, 0.6D, 0.6D, 50, 0.2D, 0.617D, 2.63D)),
                new Sphere(new Transform(new Vector3D(0, -0.25, -1), new Vector3D(0, 0, 0), new Vector3D(1, 1, 1)), 0.25D, new Reflective(new Vector3D(0, 1, 0), 0.1D, 0.6D, 0.6D, 50, 0.2D, 0.617D, 2.63D)),
                new Box(new Transform(new Vector3D(0.5, -0.25, -2.25), new Vector3D(0, 45, 0), new Vector3D(1, 1, 1)), new Vector3D(0.5, 0.5, 0.5), new TexturedMaterial(new Checker(8, 8, new Vector3D(0, 0, 0), new Vector3D(1, 1, 1)), 0.1D, 0.4D, 0.5, 32, 0.3D, 0.177, 3.638)),
                new Plane(-0.5, new Vector3D(0, 1, 0), new Reflective(new Vector3D(255/255D, 253/255D, 208/255D), 0.1D, 0.6D, 0.5D, 4, 1, 0.617D, 2.63D)),
                new Model(new Transform(new Vector3D(10, 18, -8), new Vector3D(-90, 0, 0), new Vector3D(0.1, 0.1, 0.1)), new Reflective(new Vector3D(0, 0, 1), 0.1D, 0.6D, 0.5D, 4, 1, 0.617D, 2.63D), Util.loadOBJ("./man.obj"), false)
        };

        Scene scene = new Scene(camera, ambient, lights, objects, width, height);
        scene.render("output.png", 100, 16, 16);
    }
}