import light.Light;
import material.*;
import math.Vector3D;
import object.Box;
import object.Object;
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
                new Light(new Vector3D(-2, 0.5, 0), new Vector3D(1, 1, 1), 1D),
                new Light(new Vector3D(-1, 0.5, 0), new Vector3D(1, 1, 1), 1D),
                new Light(new Vector3D(0, 0.5, 0), new Vector3D(1, 1, 1), 1D),
                new Light(new Vector3D(1, 0.5, 0), new Vector3D(1, 1, 1), 1D),
                new Light(new Vector3D(-2, 0.5, -5), new Vector3D(1, 1, 1), 1D),
                new Light(new Vector3D(-1, 0.5, -5), new Vector3D(1, 1, 1), 1D),
                new Light(new Vector3D(0, 0.5, -5), new Vector3D(1, 1, 1), 1D),
                new Light(new Vector3D(1, 0.5, -5), new Vector3D(1, 1, 1), 1D)
        };

        Object[] objects = new Object[] {
                new Sphere(new Vector3D(0, 0, -2), 0.5D, new Glass(0.1D, 0.94)),
                new Sphere(new Vector3D(-0.5, 0, -3), 0.5D, new Reflective(new Vector3D(1, 0, 0), 0.1D, 0.6D, 0.3D)),
                new Box(new Vector3D(1, -0.5, -1.5), new Vector3D(2, 0.5, -2.5), new Reflective(new Vector3D(0D, 0D, 1D), 0.1D, 0.1D, 1)),
                new Box(new Vector3D(-1.5, -0.5, -1), new Vector3D(-1, 0, -1.5), new ColoredGlass(new Vector3D(0, 1, 0), 0.1D, 0.94)),
                new Sphere(new Vector3D(0, -10000.5, -2), 10000D, new Reflective(new Vector3D(255/255D, 253/255D, 208/255D), 0.1D, 0.6D, 0.5D))
        };

        Scene scene = new Scene(camera, ambient, lights, objects, width, height);
        scene.render("output.png", 10, 8);
    }
}