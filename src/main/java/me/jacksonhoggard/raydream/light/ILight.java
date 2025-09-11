package me.jacksonhoggard.raydream.light;

import me.jacksonhoggard.raydream.math.Vector3D;

public interface ILight {
    Vector3D pointOnLight(int i, int j, int cols, int rows);

    Vector3D closestPoint(Vector3D point);

    Vector3D normalAt(Vector3D point);

    LightSample sampleLight(Vector3D p);

    double samplingWeight();

    boolean isDelta();
}
