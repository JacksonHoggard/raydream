package me.jacksonhoggard.raydream.render.sample;

import me.jacksonhoggard.raydream.math.Vector3D;

public record LightSample(
    Vector3D wi,
    Vector3D le,
    double pDir,
    boolean visible
) {}