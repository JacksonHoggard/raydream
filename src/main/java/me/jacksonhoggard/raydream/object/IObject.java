package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public interface IObject {

    Hit intersect(Ray ray);

    Vector2D mapTexture(Vector3D point);

    Vector3D calcTangent(Vector3D normal);

    Vector3D calcBitangent(Vector3D normal, Vector3D tangent);

}
