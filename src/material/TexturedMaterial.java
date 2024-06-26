package material;

import material.texture.Texture;
import math.Vector3D;
import object.Object;

public class TexturedMaterial extends Material {

    private final Texture texture;

    public TexturedMaterial(Texture texture, double ambient, double lambertian, double specular, double specularExponent, double metalness, double indexOfRefraction, double k) {
        super(null, ambient, lambertian, specular, specularExponent, metalness, indexOfRefraction, k, Type.REFLECT, true);
        this.texture = texture;
    }

    @Override
    public Vector3D getColor(Object object, Vector3D point) {
        return texture.textureAt(object, point);
    }

    public Texture getTexture() {
        return texture;
    }
}
