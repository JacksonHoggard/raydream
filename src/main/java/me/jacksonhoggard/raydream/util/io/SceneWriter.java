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

import java.io.FileWriter;
import java.io.IOException;

public class SceneWriter {

    private FileWriter writer;

    public void write(Scene scene, String path) {
        try {
            writer = new FileWriter(path);
            writeCamera(scene.getCamera());
            writer.write("\n");
            writeAmbient(scene.getAmbient());
            for(Light light : scene.getLights()) {
                writer.write("\n");
                writeLight(light);
            }
            for(Object object : scene.getObjects()) {
                writer.write("\n");
                writeObject(object);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeCamera(Camera camera) throws IOException {
        writer.write(
                "+ camera:\n" +
                        "width: " + camera.getImgWidth() + "\n" +
                        "height: " + camera.getImgHeight() + "\n" +
                        "fov: " + camera.getFov() + "\n" +
                        "aperture: " + camera.getAperture() + "\n" +
                        "from: " + vecToStr(camera.getLookFrom()) + "\n" +
                        "to: " + vecToStr(camera.getLookAt()) + "\n" +
                        "up: " + vecToStr(camera.getVUp()) + "\n" +
                        ";\n"
        );
    }

    private void writeAmbient(Light ambient) throws IOException {
        writer.write(
                "+ ambient:\n" +
                        "color: " + vecToStr(ambient.getColor()) + "\n" +
                        "brightness: " + ambient.getBrightness() + "\n" +
                        ";\n"
        );
    }

    private void writeLight(Light light) throws IOException {
        String type = null;
        if(light.getClass() == AreaLight.class)
            type = "area";
        if(light.getClass() == PointLight.class)
            type = "point";
        if(light.getClass() == SphereLight.class)
            type = "sphere";
        writer.write("+ light: " + type + "\n");
        switch(type) {
            case "area":
                writeTransform(((AreaLight) light).getTransform());
                break;
            case "sphere":
                writer.write("radius: " + ((SphereLight) light).getRadius() + "\n");
                writer.write("position: " + vecToStr(light.getPosition()) + "\n");
                break;
        }
        writer.write("color: " + vecToStr(light.getColor()) + "\n");
        writer.write("brightness: " + light.getBrightness() + "\n;\n");
    }

    private void writeObject(Object object) throws IOException {
        String type = null;
        if(object.getClass() == Sphere.class)
            type = "sphere";
        if(object.getClass() == Box.class)
            type = "box";
        if(object.getClass() == Model.class)
            type = "model";
        if(object.getClass() == Plane.class)
            type = "plane";
        writer.write("+ object: " + type + "\n");
        switch(type) {
            case "sphere":
                writeTransform(object.getTransform());
                writer.write("radius: " + ((Sphere) object).getRadius() + "\n");
                writeMaterial(object.getMaterial());
                break;
            case "box":
                writeTransform(object.getTransform());
                writer.write("size: " + vecToStr(((Box) object).getSize()) + "\n");
                writeMaterial(object.getMaterial());
                break;
            case "model":
                writeTransform(object.getTransform());
                writeMaterial(object.getMaterial());
                writer.write("mesh: " + ((Model) object).getMesh().getPath() + "\n");
                writer.write("invert: " + ((Model) object).isInvertNormals() + "\n");
                break;
            case "plane":
                writer.write("offset: " + object.getTransform().translation().y + "\n");
                writer.write("rotation: " + vecToStr(object.getTransform().rotation()) + "\n");
                writeMaterial(object.getMaterial());
                break;
        }
        writer.write(";\n");
    }

    private void writeTransform(Transform transform) throws IOException {
        writer.write(
                "transform:\n" +
                        "| translation: " + vecToStr(transform.translation()) + "\n" +
                        "| rotation: " + vecToStr(transform.rotation()) + "\n" +
                        "| scale: " + vecToStr(transform.scale()) + "\n" +
                        "/\n"
        );
    }

    private void writeMaterial(Material material) throws IOException {
        String type = null;
        if(material.getClass() == ColoredGlass.class)
            type = "colored_glass";
        if(material.getClass() == Glass.class)
            type = "glass";
        if(material.getClass() == Matte.class)
            type = "matte";
        if(material.getClass() == Reflective.class)
            type = "reflective";
        if(material.getClass() == TexturedMaterial.class)
            type = "texture";
        writer.write("material: " + type + "\n");
        switch(type) {
            case "colored_glass":
                writer.write("| color: " + vecToStr(material.getColor(null, null)) + "\n");
                writer.write("| ambient: " + material.getAmbient() + "\n");
                writer.write("| ior: " + material.getIndexOfRefraction() + "\n");
                break;
            case "glass":
                writer.write("| ambient: " + material.getAmbient() + "\n");
                writer.write("| ior: " + material.getIndexOfRefraction() + "\n");
                break;
            case "matte":
                writer.write("| color: " + vecToStr(material.getColor(null, null)) + "\n");
                writer.write("| ambient: " + material.getAmbient() + "\n");
                writer.write("| lambertian: " + material.getLambertian() + "\n");
                writer.write("| specular: " + material.getSpecular() + "\n");
                writer.write("| exponent: " + material.getSpecularExponent() + "\n");
                break;
            case "reflective":
                writer.write("| color: " + vecToStr(material.getColor(null, null)) + "\n");
                writer.write("| ambient: " + material.getAmbient() + "\n");
                writer.write("| lambertian: " + material.getLambertian() + "\n");
                writer.write("| specular: " + material.getSpecular() + "\n");
                writer.write("| exponent: " + material.getSpecularExponent() + "\n");
                writer.write("| metalness: " + material.getMetalness() + "\n");
                writer.write("| ior: " + material.getIndexOfRefraction() + "\n");
                writer.write("| k: " + material.getK() + "\n");
                break;
            case "texture":
                writeTexture(((TexturedMaterial) material).getTexture());
                writer.write("| ambient: " + material.getAmbient() + "\n");
                writer.write("| lambertian: " + material.getLambertian() + "\n");
                writer.write("| specular: " + material.getSpecular() + "\n");
                writer.write("| exponent: " + material.getSpecularExponent() + "\n");
                writer.write("| metalness: " + material.getMetalness() + "\n");
                writer.write("| ior: " + material.getIndexOfRefraction() + "\n");
                writer.write("| k: " + material.getK() + "\n");
                break;
        }
        writer.write("/\n");
    }

    private void writeTexture(Texture texture) throws IOException {
        if(texture.getClass() == Checker.class) {
            writer.write("| pattern: checker\n");
            writer.write("    | width: " + ((Checker) texture).getWidth() + "\n");
            writer.write("    | height: " + ((Checker) texture).getHeight() + "\n");
            writer.write("    | colorA: " + vecToStr(((Checker) texture).getColorA()) + "\n");
            writer.write("    | colorB: " + vecToStr(((Checker) texture).getColorB()) + "\n");
            writer.write("    /\n");
            return;
        }
        if(texture.getClass() == BoxPattern.class) {
            writer.write("| pattern: box\n");
            writer.write("    | main: " + ((BoxPattern) texture).getMain() + "\n");
            writer.write("    | ul: " + ((BoxPattern) texture).getUl() + "\n");
            writer.write("    | ur: " + ((BoxPattern) texture).getUr() + "\n");
            writer.write("    | bl: " + ((BoxPattern) texture).getBl() + "\n");
            writer.write("    | br: " + ((BoxPattern) texture).getBr() + "\n");
            writer.write("    /\n");
            return;
        }
        writer.write("| texture: " + texture.getPath() + "\n");
    }

    private String vecToStr(Vector3D v) {
        return v.x + " " + v.y + " " + v.z;
    }
}