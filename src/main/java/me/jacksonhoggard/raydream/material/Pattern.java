package me.jacksonhoggard.raydream.material;

import me.jacksonhoggard.raydream.material.texture.Texture;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Object;

public class Pattern extends Material {

    private final Texture texture;

    public Pattern(Texture texture, double ambient, double lambertian, double specular, double specularExponent, double metalness, double indexOfRefraction, double k) {
        super(new Vector3D(), ambient, lambertian, specular, specularExponent, metalness, indexOfRefraction, k, Type.REFLECT, true);
        this.texture = texture;
    }

    @Override
    public Vector3D getColor(Object object, Vector3D point) {
        return texture.textureAt(object, point);
    }
}
