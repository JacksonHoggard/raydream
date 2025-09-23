package me.jacksonhoggard.raydream.util.io;

import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.MeshModel;
import me.jacksonhoggard.raydream.gui.editor.model.RDOModel;
import me.jacksonhoggard.raydream.gui.editor.object.ModelEditorObject;
import me.jacksonhoggard.raydream.light.AreaLight;
import me.jacksonhoggard.raydream.light.PointLight;
import me.jacksonhoggard.raydream.light.SphereLight;
import me.jacksonhoggard.raydream.material.BumpMap;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.material.Texture;
import me.jacksonhoggard.raydream.material.bxdf.BxDF;
import me.jacksonhoggard.raydream.material.bxdf.disney.DisneyClearcoat;
import me.jacksonhoggard.raydream.material.bxdf.disney.DisneyDiffuse;
import me.jacksonhoggard.raydream.material.bxdf.disney.DisneyGlass;
import me.jacksonhoggard.raydream.material.bxdf.disney.DisneyMetal;
import me.jacksonhoggard.raydream.material.bxdf.disney.DisneySheen;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Box;
import me.jacksonhoggard.raydream.object.Model;
import me.jacksonhoggard.raydream.object.Plane;
import me.jacksonhoggard.raydream.object.Sphere;
import me.jacksonhoggard.raydream.object.Transform;
import me.jacksonhoggard.raydream.render.Camera;
import me.jacksonhoggard.raydream.render.Scene;
import me.jacksonhoggard.raydream.util.Util;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CLSceneReader {

    public static class Settings {
        public int width;
        public int height;
        public float fov;
        public float aperture;
        public float[] lookFrom;
        public float[] lookAt;
        public float[] up;
        public float[] sky;
        public int samples;
        public int bounces;
        public int threads;
        public int rrStartDepth;
    }

    private static class LightMaterial {
        float[] color;
        float brightness;
    }

    private static class ObjectMaterial {
        Class<? extends BxDF> bxDFClass;
        Vector3D albedo;
        Vector3D emittance;
        double indexOfRefraction;
        Texture texture;
        BumpMap bumpMap;
        double bumpScale;
        HashMap<String, Object> parameters;
    }

    public class SceneDetails {
        public final Scene scene;
        public final Settings settings;

        SceneDetails(Scene scene, Settings settings) {
            this.scene = scene;
            this.settings = settings;
        }
    }

    private final List<me.jacksonhoggard.raydream.object.Object> objects;
    private final List<me.jacksonhoggard.raydream.light.Light> lights;

    private final Settings settings;

    public CLSceneReader() {
        this.settings = new Settings();
        this.objects = new ArrayList<>();
        this.lights = new ArrayList<>();
    }

    public SceneDetails read(String path) throws IOException {
        // Extract the directory from the project file path
        Path projectPath = Paths.get(path);
        String projectDir = projectPath.getParent().toString();

        FileInputStream stream = null;
        try {
            stream = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw new IOException("Could not open file: ", e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("+")) {
                    parseObject(reader, line, projectDir);
                }
            }
        } catch (IOException e) {
            throw new IOException("Could not read file: " + path, e);
        } catch (UnrecognizedTokenException e) {
            throw new IOException("Error reading file: " + path, e);
        }
        try {
            reader.close();
        } catch (IOException e) {
            throw new IOException("Could not close stream: ", e);
        }

        me.jacksonhoggard.raydream.object.Object[] objects = new me.jacksonhoggard.raydream.object.Object[this.objects
                .size()];
        for (int i = 0; i < this.objects.size(); i++) {
            objects[i] = this.objects.get(i);
        }

        me.jacksonhoggard.raydream.light.Light[] lights = new me.jacksonhoggard.raydream.light.Light[this.lights
                .size()];
        for (int i = 0; i < this.lights.size(); i++) {
            lights[i] = this.lights.get(i);
        }

        Scene scene = new Scene(
                new Camera(
                        new Vector3D(settings.lookFrom[0], settings.lookFrom[1], settings.lookFrom[2]),
                        new Vector3D(settings.lookAt[0], settings.lookAt[1], settings.lookAt[2]),
                        new Vector3D(settings.up[0], settings.up[1], settings.up[2]),
                        settings.fov,
                        settings.aperture,
                        settings.width,
                        settings.height),
                lights,
                objects,
                settings.sky == null ? new Vector3D() : new Vector3D(settings.sky[0], settings.sky[1], settings.sky[2]),
                settings.rrStartDepth,
                settings.width,
                settings.height);
        return new SceneDetails(scene, settings);
    }

    private void parseObject(BufferedReader reader, String line, String projectDir)
            throws IOException, UnrecognizedTokenException {
        String[] tokens = line.split("\\s+");
        switch (tokens[1]) {
            case "settings:":
                setSettings(reader);
                break;
            case "light:":
                switch (tokens[2]) {
                    case "area":
                        addAreaLight(reader);
                        break;
                    case "sphere":
                        addSphereLight(reader);
                        break;
                    case "point":
                        addPointLight(reader);
                        break;
                }
                break;
            case "object:":
                switch (tokens[2]) {
                    case "sphere":
                        addSphere(reader, projectDir);
                        break;
                    case "box":
                        addBox(reader, projectDir);
                        break;
                    case "plane":
                        addPlane(reader, projectDir);
                        break;
                    case "model":
                        addModel(reader, projectDir);
                        break;
                }
                break;
            default:
                throw new UnrecognizedTokenException(tokens[1]);
        }
    }

    private void setSettings(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        String line;
        while (!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch (params[0]) {
                case "width:":
                    settings.width = Integer.parseInt(params[1]);
                    break;
                case "height:":
                    settings.height = Integer.parseInt(params[1]);
                    break;
                case "fov:":
                    settings.fov = Float.parseFloat(params[1]);
                    break;
                case "aperture:":
                    settings.aperture = Float.parseFloat(params[1]);
                    break;
                case "lookFrom:":
                    settings.lookFrom = new float[] {
                            Float.parseFloat(params[1]),
                            Float.parseFloat(params[2]),
                            Float.parseFloat(params[3])
                    };
                    break;
                case "lookAt:":
                    settings.lookAt = new float[] {
                            Float.parseFloat(params[1]),
                            Float.parseFloat(params[2]),
                            Float.parseFloat(params[3])
                    };
                    break;
                case "up:":
                    settings.up = new float[] {
                            Float.parseFloat(params[1]),
                            Float.parseFloat(params[2]),
                            Float.parseFloat(params[3])
                    };
                    break;
                case "sky:":
                    settings.sky = new float[] {
                            Float.parseFloat(params[1]),
                            Float.parseFloat(params[2]),
                            Float.parseFloat(params[3])
                    };
                    break;
                case "samples:":
                    settings.samples = Integer.parseInt(params[1]);
                    break;
                case "bounces:":
                    settings.bounces = Integer.parseInt(params[1]);
                    break;
                case "threads:":
                    settings.threads = Integer.parseInt(params[1]);
                    break;
                case "rrStartDepth:":
                    settings.rrStartDepth = Integer.parseInt(params[1]);
                    break;
            }
        }
    }

    private void parseLightMaterial(BufferedReader reader, LightMaterial material)
            throws IOException, UnrecognizedTokenException {
        float[] color = new float[3];
        float brightness = 0;
        String line;
        while (!(line = reader.readLine()).trim().startsWith("/") && line.trim().startsWith("|")) {
            String[] params = line.split("\\s+");
            switch (params[1]) {
                case "color:":
                    color[0] = Float.parseFloat(params[2]);
                    color[1] = Float.parseFloat(params[3]);
                    color[2] = Float.parseFloat(params[4]);
                    break;
                case "brightness:":
                    brightness = Float.parseFloat(params[2]);
                    break;
            }
        }
        material.color = color;
        material.brightness = brightness;
    }

    private void addAreaLight(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        LightMaterial material = new LightMaterial();
        String line;
        while (!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch (params[0]) {
                case "transform:":
                    parseTransform(reader, translation, rotation, scale);
                    break;
                case "material:":
                    parseLightMaterial(reader, material);
                    break;
            }
        }
        lights.add(
                new AreaLight(
                        new Transform(
                                new Vector3D(translation[0], translation[1], translation[2]),
                                new Vector3D(rotation[0], rotation[1], rotation[2]),
                                new Vector3D(scale[0], scale[1], scale[2])),
                        new Vector3D(material.color[0], material.color[1], material.color[2]),
                        (double) material.brightness));
    }

    private void addSphereLight(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        LightMaterial material = new LightMaterial();
        String line;
        while (!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch (params[0]) {
                case "transform:":
                    parseTransform(reader, translation, rotation, scale);
                    break;
                case "material:":
                    parseLightMaterial(reader, material);
                    break;
            }
        }
        lights.add(
                new SphereLight(
                        new Vector3D(translation[0], translation[1], translation[2]),
                        new Vector3D(material.color[0], material.color[1], material.color[2]),
                        (double) material.brightness,
                        (double) scale[0]));
    }

    private void addPointLight(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        LightMaterial material = new LightMaterial();
        String line;
        while (!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch (params[0]) {
                case "transform:":
                    parseTransform(reader, translation, rotation, scale);
                    break;
                case "material:":
                    parseLightMaterial(reader, material);
                    break;
            }
        }
        lights.add(
                new PointLight(
                        new Vector3D(translation[0], translation[1], translation[2]),
                        new Vector3D(material.color[0], material.color[1], material.color[2]),
                        (double) material.brightness));
    }

    private void addSphere(BufferedReader reader, String projectDir) throws IOException, UnrecognizedTokenException {
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        ObjectMaterial material = new ObjectMaterial();
        String line;
        while (!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch (params[0]) {
                case "transform:":
                    parseTransform(reader, translation, rotation, scale);
                    break;
                case "material:":
                    material = parseObjectMaterial(reader, projectDir);
                    break;
            }
        }
        objects.add(new Sphere(
                new Transform(
                        new Vector3D(translation[0], translation[1], translation[2]),
                        new Vector3D(rotation[0], rotation[1], rotation[2]),
                        new Vector3D(scale[0], scale[1], scale[2])),
                1.0D,
                Material.of(material.bxDFClass,
                        material.albedo != null ? material.albedo : new Vector3D(1, 1, 1),
                        material.emittance != null ? material.emittance : new Vector3D(0, 0, 0),
                        material.indexOfRefraction,
                        material.texture,
                        material.bumpMap,
                        material.parameters)));
    }

    private void addBox(BufferedReader reader, String projectDir) throws IOException, UnrecognizedTokenException {
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        ObjectMaterial material = new ObjectMaterial();
        String line;
        while (!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch (params[0]) {
                case "transform:":
                    parseTransform(reader, translation, rotation, scale);
                    break;
                case "material:":
                    material = parseObjectMaterial(reader, projectDir);
                    break;
            }
        }
        objects.add(
                new Box(
                        new Transform(new Vector3D(translation[0], translation[1], translation[2]),
                                new Vector3D(rotation[0], rotation[1], rotation[2]),
                                new Vector3D(scale[0], scale[1], scale[2])),
                        new Vector3D(1, 1, 1),
                        Material.of(material.bxDFClass,
                                material.albedo != null ? material.albedo : new Vector3D(1, 1, 1),
                                material.emittance != null ? material.emittance : new Vector3D(0, 0, 0),
                                material.indexOfRefraction,
                                material.texture,
                                material.bumpMap,
                                material.parameters)));
    }

    private void addPlane(BufferedReader reader, String projectDir) throws IOException, UnrecognizedTokenException {
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        ObjectMaterial material = new ObjectMaterial();
        String line;
        while (!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch (params[0]) {
                case "transform:":
                    parseTransform(reader, translation, rotation, scale);
                    break;
                case "material:":
                    material = parseObjectMaterial(reader, projectDir);
                    break;
            }
        }
        objects.add(
                new Plane(
                        translation[1],
                        new Vector3D(rotation[0], rotation[1], rotation[2]),
                        Material.of(material.bxDFClass,
                                material.albedo != null ? material.albedo : new Vector3D(1, 1, 1),
                                material.emittance != null ? material.emittance : new Vector3D(0, 0, 0),
                                material.indexOfRefraction,
                                material.texture,
                                material.bumpMap,
                                material.parameters)));
    }

    private void addModel(BufferedReader reader, String projectDir) throws IOException, UnrecognizedTokenException {
        List<ObjectMaterial> materials = new ArrayList<>();
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        StringBuilder label = new StringBuilder();
        RDOModel model = null;
        String line;
        while (!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch (params[0]) {
                case "label:":
                    label.append(line.substring(7));
                    break;
                case "transform:":
                    parseTransform(reader, translation, rotation, scale);
                    break;
                case "material:":
                    ObjectMaterial material = parseObjectMaterial(reader, projectDir);
                    materials.add(material);
                    break;
                case "file:":
                    String path = projectDir + File.separator + line.substring(6);
                    model = new RDOModel(path, new FileInputStream(path));
                    model.loadRDO();
                    break;
            }
        }
        ModelEditorObject modelEditorObject = new ModelEditorObject(model, translation, rotation, scale,
                label.toString());
        if (!materials.isEmpty()) {
            int i = 0;
            for (MeshModel.Mesh mesh : ((MeshModel) modelEditorObject.getModel()).getMeshes()) {
                mesh.setMaterial(EditorObjectMaterial.of(materials.get(i).bxDFClass,
                        materials.get(i).albedo != null ? new float[] {
                                (float) materials.get(i).albedo.x,
                                (float) materials.get(i).albedo.y,
                                (float) materials.get(i).albedo.z
                        } : new float[] { 1, 1, 1 },
                        materials.get(i).emittance != null ? new float[] {
                                (float) materials.get(i).emittance.x,
                                (float) materials.get(i).emittance.y,
                                (float) materials.get(i).emittance.z
                        } : new float[] { 0, 0, 0 },
                        (float) materials.get(i).indexOfRefraction,
                        (float) materials.get(i).bumpScale,
                        materials.get(i).parameters));
                i++;
            }
        }
        Model[] models = modelEditorObject.toObjects();
        Model[] newModels = new Model[models.length];
        for(int i = 0; i < models.length; i++) {
            Model m = models[i];
            newModels[i] = new Model(
                m.getTransform(),
                Material.of(
                    materials.get(i).bxDFClass,
                    materials.get(i).albedo != null ? materials.get(i).albedo : new Vector3D(1, 1, 1),
                    materials.get(i).emittance != null ? materials.get(i).emittance : new Vector3D(0, 0, 0),
                    materials.get(i).indexOfRefraction,
                    materials.get(i).texture,
                    materials.get(i).bumpMap,
                    materials.get(i).parameters
                ),
                m.getMesh()
            );
        }
        objects.addAll(List.of(newModels));
    }

    public ObjectMaterial parseObjectMaterial(BufferedReader reader, String dirPath)
            throws IOException, UnrecognizedTokenException {
        String line;
        String bumpMapPath = null;
        ObjectMaterial material = new ObjectMaterial();
        HashMap<String, Object> parameters = new HashMap<>();
        while (!(line = reader.readLine()).trim().startsWith("/") && line.trim().startsWith("|")) {
            String[] params = line.trim().split("\\s+");
            switch (params[1]) {
                case "type:":
                    switch (params[2]) {
                        case "DisneyDiffuse":
                            material.bxDFClass = (Class) DisneyDiffuse.class;
                            break;
                        case "DisneyMetal":
                            material.bxDFClass = (Class) DisneyMetal.class;
                            break;
                        case "DisneySheen":
                            material.bxDFClass = (Class) DisneySheen.class;
                            break;
                        case "DisneyClearcoat":
                            material.bxDFClass = (Class) DisneyClearcoat.class;
                            break;
                        case "DisneyGlass":
                            material.bxDFClass = (Class) DisneyGlass.class;
                            break;
                    }
                    break;
                case "albedo:":
                    material.albedo = new Vector3D(
                            Float.parseFloat(params[2]),
                            Float.parseFloat(params[3]),
                            Float.parseFloat(params[4]));
                    break;
                case "emittance:":
                    material.emittance = new Vector3D(
                            Float.parseFloat(params[2]),
                            Float.parseFloat(params[3]),
                            Float.parseFloat(params[4]));
                    break;
                case "texture:":
                    if (params[2].equals("null"))
                        break;
                    String texturePath = dirPath + File.separator + line.substring(11);
                    texturePath = texturePath.replace("/", File.separator);
                    texturePath = texturePath.replace("\\", File.separator);
                    material.texture = Util.loadTexture(texturePath);
                    break;
                case "bump:":
                    if (params[2].equals("null"))
                        break;
                    bumpMapPath = dirPath + File.separator + line.substring(8);
                    bumpMapPath = bumpMapPath.replace("/", File.separator);
                    bumpMapPath = bumpMapPath.replace("\\", File.separator);
                    break;
                case "bScale:":
                    material.bumpScale = Float.parseFloat(params[2]);
                    break;
                case "indexOfRefraction:":
                    material.indexOfRefraction = Float.parseFloat(params[2]);
                    break;
                case "parameters:":
                    parameters = parseParameters(reader);
                    break;
            }
        }
        material.parameters = parameters;
        if (bumpMapPath != null) {
            material.bumpMap = Util.loadBumpMap(bumpMapPath, material.bumpScale);
        }
        return material;
    }

    private static HashMap<String, Object> parseParameters(BufferedReader reader)
            throws IOException, UnrecognizedTokenException {
        HashMap<String, Object> parameters = new HashMap<>();
        String line;
        while (!(line = reader.readLine()).trim().startsWith("/") && line.trim().startsWith("|")) {
            String[] params = line.trim().split("\\s+");
            parameters.put(params[1].replace(":", ""), Double.parseDouble(params[2]));
        }
        return parameters;
    }

    private static void parseTransform(BufferedReader reader, float[] translation, float[] rotation, float[] scale)
            throws IOException, UnrecognizedTokenException {
        String line;
        while (!(line = reader.readLine()).trim().startsWith("/") && line.trim().startsWith("|")) {
            String[] params = line.trim().split("\\s+");
            switch (params[1]) {
                case "scale:":
                    scale[0] = Float.parseFloat(params[2]);
                    scale[1] = Float.parseFloat(params[3]);
                    scale[2] = Float.parseFloat(params[4]);
                    break;
                case "translation:":
                    translation[0] = Float.parseFloat(params[2]);
                    translation[1] = Float.parseFloat(params[3]);
                    translation[2] = Float.parseFloat(params[4]);
                    break;
                case "rotation:":
                    rotation[0] = Float.parseFloat(params[2]);
                    rotation[1] = Float.parseFloat(params[3]);
                    rotation[2] = Float.parseFloat(params[4]);
                    break;
            }
        }
    }

}
