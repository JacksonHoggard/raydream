package me.jacksonhoggard.raydream.util.io;

import me.jacksonhoggard.raydream.light.AreaLight;
import me.jacksonhoggard.raydream.light.Light;
import me.jacksonhoggard.raydream.light.PointLight;
import me.jacksonhoggard.raydream.light.SphereLight;
import me.jacksonhoggard.raydream.material.*;
import me.jacksonhoggard.raydream.material.texture.BoxPattern;
import me.jacksonhoggard.raydream.material.texture.Checker;
import me.jacksonhoggard.raydream.material.texture.Texture;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.*;
import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.render.Camera;
import me.jacksonhoggard.raydream.render.Scene;
import me.jacksonhoggard.raydream.util.Util;

import java.io.*;
import java.util.ArrayList;

public class SceneReader {

    private Camera camera;
    private Light ambient;
    private ArrayList<Light> lights;
    private ArrayList<Object> objects;
    private int width;
    private int height;

    public SceneReader() {
        this.lights = new ArrayList<>();
        this.objects = new ArrayList<>();
    }

    public Scene read(String path) {
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        try {
            while((line = reader.readLine()) != null) {
                if(line.startsWith("+")) {
                    parseObject(reader, line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnrecognizedTokenException e) {
            throw new RuntimeException(e);
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Object[] objArr = new Object[objects.size()];
        objArr = objects.toArray(objArr);
        Light[] lightArr = new Light[lights.size()];
        lightArr = lights.toArray(lightArr);
        return new Scene(camera, ambient, lightArr, objArr, new Vector3D(0, 0, 0), width, height);
    }

    private void parseObject(BufferedReader reader, String line) throws IOException, UnrecognizedTokenException {
        String[] tokens = line.split("\\s+");
        switch(tokens[1]) {
            case "camera:":
                camera = createCamera(reader);
                break;
            case "ambient:":
                ambient = createAmbient(reader);
                break;
            case "light:":
                switch(tokens[2]) {
                    case "area":
                        lights.add(createAreaLight(reader));
                        break;
                    case "sphere":
                        lights.add(createSphereLight(reader));
                        break;
                    default:
                        throw new UnrecognizedTokenException(tokens[2]);
                }
                break;
            case "object:":
                switch(tokens[2]) {
                    case "sphere":
                        objects.add(createSphere(reader));
                        break;
                    case "box":
                        objects.add(createBox(reader));
                        break;
                    case "plane":
                        objects.add(createPlane(reader));
                        break;
                    case "model":
                        objects.add(createModel(reader));
                        break;
                    default:
                        throw new UnrecognizedTokenException(tokens[2]);
                }
                break;
            default:
                throw new UnrecognizedTokenException(tokens[1]);
        }
    }

    private Camera createCamera(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        Vector3D lookFrom = null;
        Vector3D lookAt = null;
        double fov = 0;
        double aperture = 0;
        Vector3D up = null;
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "width:":
                    width = Integer.parseInt(params[1]);
                    break;
                case "height:":
                    height = Integer.parseInt(params[1]);
                    break;
                case "fov:":
                    fov = Double.parseDouble(params[1]);
                    break;
                case "aperture:":
                    aperture = Double.parseDouble(params[1]);
                    break;
                case "from:":
                    lookFrom = new Vector3D(
                            Double.parseDouble(params[1]),
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3])
                    );
                    break;
                case "to:":
                    lookAt = new Vector3D(
                            Double.parseDouble(params[1]),
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3])
                    );
                    break;
                case "up:":
                    up = new Vector3D(
                            Double.parseDouble(params[1]),
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3])
                    );
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        return new Camera(lookFrom, lookAt, up, fov, aperture, width, height);
    }

    private Light createAmbient(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        Vector3D color = null;
        double brightness = 0;
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "color:":
                    color = new Vector3D(
                            Double.parseDouble(params[1]),
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3])
                    );
                    break;
                case "brightness:":
                    brightness = Double.parseDouble(params[1]);
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        return new PointLight(new Vector3D(), color, brightness);
    }

    private AreaLight createAreaLight(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        Transform transform = null;
        Vector3D color = null;
        double brightness = 0;
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "transform:":
                    transform = createTransform(reader);
                    break;
                case "color:":
                    color = new Vector3D(
                            Double.parseDouble(params[1]),
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3])
                    );
                    break;
                case "brightness:":
                    brightness = Double.parseDouble(params[1]);
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        return new AreaLight(transform, color, brightness);
    }

    private SphereLight createSphereLight(BufferedReader reader) throws IOException {
        Vector3D position = null;
        Vector3D color = null;
        double brightness = 0;
        double radius = 0;
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "position:":
                    position = new Vector3D(
                            Double.parseDouble(params[1]),
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3])
                    );
                    break;
                case "color:":
                    color = new Vector3D(
                            Double.parseDouble(params[1]),
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3])
                    );
                    break;
                case "brightness:":
                    brightness = Double.parseDouble(params[1]);
                    break;
                case "radius:":
                    radius = Double.parseDouble(params[1]);
                    break;
            }
        }
        return new SphereLight(position, color, brightness, radius);
    }

    private Sphere createSphere(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        Transform transform = null;
        double radius = 0;
        Material material = null;
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "transform:":
                    transform = createTransform(reader);
                    break;
                case "radius:":
                    radius = Double.parseDouble(params[1]);
                    break;
                case "material:":
                    material = createMaterial(reader, params[1]);
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        return new Sphere(transform, radius, material);
    }

    private Box createBox(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        Transform transform = null;
        Vector3D size = null;
        Material material = null;
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "transform:":
                    transform = createTransform(reader);
                    break;
                case "size:":
                    size = new Vector3D(
                            Double.parseDouble(params[1]),
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3])
                    );
                    break;
                case "material:":
                    material = createMaterial(reader, params[1]);
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        return new Box(transform, size, material);
    }

    private Plane createPlane(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        double offset = 0;
        Vector3D rotation = null;
        Material material = null;
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "offset:":
                    offset = Double.parseDouble(params[1]);
                    break;
                case "rotation:":
                    rotation = new Vector3D(
                            Double.parseDouble(params[1]),
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3])
                    );
                    break;
                case "material:":
                    material = createMaterial(reader, params[1]);
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        return new Plane(offset, rotation, material);
    }

    private Model createModel(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        Transform transform = null;
        Material material = null;
        Mesh mesh = null;
        boolean invert = false;
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "transform:":
                    transform = createTransform(reader);
                    break;
                case "material:":
                    material = createMaterial(reader, params[1]);
                    break;
                case "mesh:":
                    mesh = Util.loadOBJ(line.substring(6));
                    break;
                case "invert:":
                    invert = Boolean.parseBoolean(params[1]);
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        return new Model(transform, material, mesh, invert);
    }

    private Material createMaterial(BufferedReader reader, String type) throws IOException, UnrecognizedTokenException {
        Vector3D color = null;
        Texture texture = null;
        double ambient = 0;
        double lambertian = 0;
        double specular = 0;
        double specularExponent = 0;
        double metalness = 0;
        double indexOfRefraction = 0;
        double k = 0;
        String line;
        while(!(line = reader.readLine()).trim().startsWith("/") && line.trim().startsWith("|")) {
            String[] params = line.trim().split("\\s+");
            switch(params[1]) {
                case "color:":
                    color = new Vector3D(
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3]),
                            Double.parseDouble(params[4])
                    );
                    break;
                case "texture:":
                    if(texture != null)
                        throw new IllegalArgumentException("Texture/pattern already assigned.");
                    texture = Util.loadTexture(line.trim().substring(11));
                    break;
                case "pattern:":
                    if(texture != null)
                        throw new IllegalArgumentException("Texture/pattern already assigned.");
                    texture = createPattern(reader, params[2]);
                    break;
                case "ambient:":
                    ambient = Double.parseDouble(params[2]);
                    break;
                case "lambertian:":
                    lambertian = Double.parseDouble(params[2]);
                    break;
                case "specular:":
                    specular = Double.parseDouble(params[2]);
                    break;
                case "exponent:":
                    specularExponent = Double.parseDouble(params[2]);
                    break;
                case "metalness:":
                    metalness = Double.parseDouble(params[2]);
                    break;
                case "ior:":
                    indexOfRefraction = Double.parseDouble(params[2]);
                    break;
                case "k:":
                    k = Double.parseDouble(params[2]);
                    break;
                default:
                    throw new UnrecognizedTokenException(params[1]);
            }
        }
        return switch (type) {
            case "colored_glass" -> new ColoredGlass(color, ambient, indexOfRefraction);
            case "glass" -> new Glass(ambient, indexOfRefraction);
            case "matte" -> new Matte(color, ambient, lambertian, specular, specularExponent);
            case "reflective" -> new Reflective(color, ambient, lambertian, specular, specularExponent, metalness, indexOfRefraction, k);
            case "texture" -> new TexturedMaterial(texture, ambient, lambertian, specular, specularExponent, metalness, indexOfRefraction, k);
            default -> throw new UnrecognizedTokenException(type);
        };
    }

    private Texture createPattern(BufferedReader reader, String type) throws IOException, UnrecognizedTokenException {
        int width = 0;
        int height = 0;
        Vector3D colorA = null;
        Vector3D colorB = null;
        Vector3D main = null;
        Vector3D ul = null;
        Vector3D ur = null;
        Vector3D bl = null;
        Vector3D br = null;
        String line;
        while(!(line = reader.readLine()).trim().startsWith("/") && line.trim().startsWith("|")) {
            String[] params = line.trim().split("\\s+");
            switch(params[1]) {
                case "colorA:":
                    colorA = new Vector3D(
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3]),
                            Double.parseDouble(params[4])
                    );
                    break;
                case "colorB:":
                    colorB = new Vector3D(
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3]),
                            Double.parseDouble(params[4])
                    );
                    break;
                case "width:":
                    width = Integer.parseInt(params[2]);
                    break;
                case "height:":
                    height = Integer.parseInt(params[2]);
                    break;
                case "main:":
                    main = new Vector3D(
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3]),
                            Double.parseDouble(params[4])
                    );
                    break;
                case "ul:":
                    ul = new Vector3D(
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3]),
                            Double.parseDouble(params[4])
                    );
                    break;
                case "ur:":
                    ur = new Vector3D(
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3]),
                            Double.parseDouble(params[4])
                    );
                    break;
                case "bl:":
                    bl = new Vector3D(
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3]),
                            Double.parseDouble(params[4])
                    );
                    break;
                case "br:":
                    br = new Vector3D(
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3]),
                            Double.parseDouble(params[4])
                    );
                    break;
                default:
                    throw new UnrecognizedTokenException(params[1]);
            }
        }
        return switch (type) {
            case "checker" -> new Checker(width, height, colorA, colorB);
            case "box" -> new BoxPattern(main, ul, ur, bl, br);
            default -> throw new UnrecognizedTokenException(type);
        };
    }

    private Transform createTransform(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        Vector3D translation = null;
        Vector3D scale = null;
        Vector3D rotation = null;
        String line;
        while(!(line = reader.readLine()).trim().startsWith("/") && line.trim().startsWith("|")) {
            String[] params = line.trim().split("\\s+");
            switch(params[1]) {
                case "scale:":
                    scale = new Vector3D(
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3]),
                            Double.parseDouble(params[4])
                    );
                    break;
                case "translation:":
                    translation = new Vector3D(
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3]),
                            Double.parseDouble(params[4])
                    );
                    break;
                case "rotation:":
                    rotation = new Vector3D(
                            Double.parseDouble(params[2]),
                            Double.parseDouble(params[3]),
                            Double.parseDouble(params[4])
                    );
                    break;
                default:
                    throw new UnrecognizedTokenException(params[1]);
            }
        }
        return new Transform(translation, rotation, scale);
    }

}
