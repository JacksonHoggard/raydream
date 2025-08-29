package me.jacksonhoggard.raydream.light;

import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector3D;

public interface ILight {
    double intersect(Ray ray);

    Vector3D pointOnLight(int i, int j, int cols, int rows);

    Vector3D closestPoint(Vector3D point);
}
