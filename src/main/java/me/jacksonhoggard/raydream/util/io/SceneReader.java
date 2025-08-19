package me.jacksonhoggard.raydream.util.io;

import me.jacksonhoggard.raydream.gui.editor.light.EditorAreaLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorPointLight;
import me.jacksonhoggard.raydream.gui.editor.light.EditorSphereLight;
import me.jacksonhoggard.raydream.gui.editor.material.EditorLightMaterial;
import me.jacksonhoggard.raydream.gui.editor.material.EditorObjectMaterial;
import me.jacksonhoggard.raydream.gui.editor.model.MeshModel;
import me.jacksonhoggard.raydream.gui.editor.model.RDOModel;
import me.jacksonhoggard.raydream.gui.editor.object.BoxEditorObject;
import me.jacksonhoggard.raydream.gui.editor.object.ModelEditorObject;
import me.jacksonhoggard.raydream.gui.editor.object.PlaneEditorObject;
import me.jacksonhoggard.raydream.gui.editor.object.SphereEditorObject;
import me.jacksonhoggard.raydream.gui.editor.window.ObjectWindow;
import me.jacksonhoggard.raydream.gui.editor.window.SettingsWindow;
import me.jacksonhoggard.raydream.material.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SceneReader {

    public static void read(String path) throws IOException {
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
            while((line = reader.readLine()) != null) {
                if(line.startsWith("+")) {
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
    }

    private static void parseObject(BufferedReader reader, String line, String projectDir) throws IOException, UnrecognizedTokenException {
        String[] tokens = line.split("\\s+");
        switch(tokens[1]) {
            case "settings:":
                setSettings(reader);
                break;
            case "light:":
                switch(tokens[2]) {
                    case "area":
                        addAreaLight(reader);
                        break;
                    case "sphere":
                        addSphereLight(reader);
                        break;
                    case "point":
                        addPointLight(reader);
                        break;
                    default:
                        throw new UnrecognizedTokenException(tokens[2]);
                }
                break;
            case "object:":
                switch(tokens[2]) {
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
                    default:
                        throw new UnrecognizedTokenException(tokens[2]);
                }
                break;
            default:
                throw new UnrecognizedTokenException(tokens[1]);
        }
    }

    private static void setSettings(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "width:":
                    SettingsWindow.setImgWidth(Integer.parseInt(params[1]));
                    break;
                case "height:":
                    SettingsWindow.setImgHeight(Integer.parseInt(params[1]));
                    break;
                case "fov:":
                    SettingsWindow.setFov(Float.parseFloat(params[1]));
                    break;
                case "aperture:":
                    SettingsWindow.setAperture(Float.parseFloat(params[1]));
                    break;
                case "lookFrom:":
                    SettingsWindow.setLookFrom(new float[] {
                            Float.parseFloat(params[1]),
                            Float.parseFloat(params[2]),
                            Float.parseFloat(params[3])
                    });
                    break;
                case "lookAt:":
                    SettingsWindow.setLookAt(new float[]{
                            Float.parseFloat(params[1]),
                            Float.parseFloat(params[2]),
                            Float.parseFloat(params[3])
                    });
                    break;
                case "up:":
                    SettingsWindow.setUp(new float[]{
                            Float.parseFloat(params[1]),
                            Float.parseFloat(params[2]),
                            Float.parseFloat(params[3])
                    });
                    break;
                case "sky:":
                    SettingsWindow.setSkyColor(new float[] {
                            Float.parseFloat(params[1]),
                            Float.parseFloat(params[2]),
                            Float.parseFloat(params[3])
                    });
                    break;
                case "ambient:":
                    SettingsWindow.setAmbientColor(new float[] {
                            Float.parseFloat(params[1]),
                            Float.parseFloat(params[2]),
                            Float.parseFloat(params[3])
                    });
                    break;
                case "samples:":
                    SettingsWindow.setSampleDepth(Integer.parseInt(params[1]));
                    break;
                case "bounces:":
                    SettingsWindow.setBounces(Integer.parseInt(params[1]));
                    break;
                case "shadows:":
                    SettingsWindow.setNumShadowRays(Integer.parseInt(params[1]));
                    break;
                case "threads:":
                    SettingsWindow.setThreads(Integer.parseInt(params[1]));
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
    }

    private static void parseLightMaterial(BufferedReader reader, EditorLightMaterial material) throws IOException, UnrecognizedTokenException {
        float[] color = new float[3];
        float brightness = 0;
        String line;
        while(!(line = reader.readLine()).trim().startsWith("/") && line.trim().startsWith("|")) {
            String[] params = line.split("\\s+");
            switch(params[1]) {
                case "color:":
                    color[0] = Float.parseFloat(params[2]);
                    color[1] = Float.parseFloat(params[3]);
                    color[2] = Float.parseFloat(params[4]);
                    break;
                case "brightness:":
                    brightness = Float.parseFloat(params[2]);
                    break;
                default:
                    throw new UnrecognizedTokenException(params[1]);
            }
        }
        material.setColor(color);
        material.setBrightness(brightness);
    }

    private static void addAreaLight(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        EditorLightMaterial material = new EditorLightMaterial();
        StringBuilder label = new StringBuilder();
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "label:":
                    label.append(line.substring(7));
                    break;
                case "transform:":
                    parseTransform(reader, translation, rotation, scale);
                    break;
                case "material:":
                    parseLightMaterial(reader, material);
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        ObjectWindow.lights.add(new EditorAreaLight(translation, rotation, scale, material, label.toString()));
    }

    private static void addSphereLight(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        EditorLightMaterial material = new EditorLightMaterial();
        StringBuilder label = new StringBuilder();
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "label:":
                    label.append(line.substring(7));
                    break;
                case "transform:":
                    parseTransform(reader, translation, rotation, scale);
                    break;
                case "material:":
                    parseLightMaterial(reader, material);
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        ObjectWindow.lights.add(new EditorSphereLight(translation, rotation, scale, material, label.toString()));
    }

    private static void addPointLight(BufferedReader reader) throws IOException, UnrecognizedTokenException {
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        EditorLightMaterial material = new EditorLightMaterial();
        StringBuilder label = new StringBuilder();
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "label:":
                    label.append(line.substring(7));
                    break;
                case "transform:":
                    parseTransform(reader, translation, rotation, scale);
                    break;
                case "material:":
                    parseLightMaterial(reader, material);
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        ObjectWindow.lights.add(new EditorPointLight(translation, rotation, scale, material, label.toString()));
    }

    private static void addSphere(BufferedReader reader, String projectDir) throws IOException, UnrecognizedTokenException {
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        EditorObjectMaterial material = new EditorObjectMaterial();
        StringBuilder label = new StringBuilder();
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "label:":
                    label.append(line.substring(7));
                    break;
                case "transform:":
                    parseTransform(reader, translation, rotation, scale);
                    break;
                case "material:":
                    parseObjectMaterial(reader, material, projectDir);
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        ObjectWindow.objects.add(new SphereEditorObject(translation, rotation, scale, material, label.toString()));
    }

    private static void addBox(BufferedReader reader, String projectDir) throws IOException, UnrecognizedTokenException {
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        EditorObjectMaterial material = new EditorObjectMaterial();
        StringBuilder label = new StringBuilder();
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "label:":
                    label.append(line.substring(7));
                    break;
                case "transform:":
                    parseTransform(reader, translation, rotation, scale);
                    break;
                case "material:":
                    parseObjectMaterial(reader, material, projectDir);
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        ObjectWindow.objects.add(new BoxEditorObject(translation, rotation, scale, material, label.toString()));
    }

    private static void addPlane(BufferedReader reader, String projectDir)  throws IOException, UnrecognizedTokenException {
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        EditorObjectMaterial material = new EditorObjectMaterial();
        StringBuilder label = new StringBuilder();
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "label:":
                    label.append(line.substring(7));
                    break;
                case "transform:":
                    parseTransform(reader, translation, rotation, scale);
                    break;
                case "material:":
                    parseObjectMaterial(reader, material, projectDir);
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        ObjectWindow.objects.add(new PlaneEditorObject(translation, rotation, scale, material, label.toString()));
    }

    private static void addModel(BufferedReader reader, String projectDir)  throws IOException, UnrecognizedTokenException {
        List<EditorObjectMaterial> materials = new ArrayList<>();
        float[] translation = new float[3];
        float[] rotation = new float[3];
        float[] scale = new float[3];
        StringBuilder label = new StringBuilder();
        RDOModel model = null;
        String line;
        while(!(line = reader.readLine()).startsWith(";")) {
            String[] params = line.split("\\s+");
            switch(params[0]) {
                case "label:":
                    label.append(line.substring(7));
                    break;
                case "transform:":
                    parseTransform(reader, translation, rotation, scale);
                    break;
                case "material:":
                    EditorObjectMaterial material = new EditorObjectMaterial();
                    parseObjectMaterial(reader, material, projectDir);
                    materials.add(material);
                    break;
                case "file:":
                    model = new RDOModel(projectDir + File.separator + line.substring(6), new FileInputStream(projectDir + File.separator + line.substring(6)));
                    break;
                default:
                    throw new UnrecognizedTokenException(params[0]);
            }
        }
        ModelEditorObject modelEditorObject = new ModelEditorObject(model, translation, rotation, scale, label.toString());
        if(!materials.isEmpty()) {
            int i = 0;
            for (MeshModel.Mesh mesh : ((MeshModel) modelEditorObject.getModel()).getMeshes()) {
                mesh.setMaterial(materials.get(i));
                i++;
            }
        }
        ObjectWindow.objects.add(modelEditorObject);
    }

    public static void parseObjectMaterial(BufferedReader reader, EditorObjectMaterial material, String dirPath) throws IOException, UnrecognizedTokenException {
        String line;
        while(!(line = reader.readLine()).trim().startsWith("/") && line.trim().startsWith("|")) {
            String[] params = line.trim().split("\\s+");
            switch(params[1]) {
                case "color:":
                    material.setColor(new float[] {
                            Float.parseFloat(params[2]),
                            Float.parseFloat(params[3]),
                            Float.parseFloat(params[4])
                    });
                    break;
                case "ambient:":
                    material.setAmbient(Float.parseFloat(params[2]));
                    break;
                case "diffuse:":
                     material.setDiffuse(Float.parseFloat(params[2]));
                    break;
                case "specular:":
                    material.setSpecular(Float.parseFloat(params[2]));
                    break;
                case "exponent:":
                    material.setSpecularExponent(Float.parseFloat(params[2]));
                    break;
                case "metalness:":
                    material.setMetalness(Float.parseFloat(params[2]));
                    break;
                case "roughness:":
                    material.setRoughness(Float.parseFloat(params[2]));
                    break;
                case "ior:":
                    material.setIndexOfRefraction(Float.parseFloat(params[2]));
                    break;
                case "k:":
                    material.setK(Float.parseFloat(params[2]));
                    break;
                case "type:":
                    material.setType(Material.Type.valueOf(params[2]));
                    break;
                case "texture:":
                    if(params[2].equals("null"))
                        break;
                    material.setTexture(new me.jacksonhoggard.raydream.gui.editor.material.Texture(dirPath + File.separator + line.substring(11)));
                    break;
                case "bump:":
                    if(params[2].equals("null"))
                        break;
                    material.setBumpMap(new me.jacksonhoggard.raydream.gui.editor.material.Texture(dirPath + File.separator + line.substring(8)));
                    break;
                case "bScale:":
                    material.setBumpScale(Float.parseFloat(params[2]));
                    break;
                default:
                    throw new UnrecognizedTokenException(params[1]);
            }
        }
    }

    private static void parseTransform(BufferedReader reader, float[] translation, float[] rotation, float[] scale) throws IOException, UnrecognizedTokenException {
        String line;
        while(!(line = reader.readLine()).trim().startsWith("/") && line.trim().startsWith("|")) {
            String[] params = line.trim().split("\\s+");
            switch(params[1]) {
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
                default:
                    throw new UnrecognizedTokenException(params[1]);
            }
        }
    }

}
