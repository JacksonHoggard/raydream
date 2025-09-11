package me.jacksonhoggard.raydream.light;

import me.jacksonhoggard.raydream.math.Vector3D;

public record LightSample(Vector3D wi, Vector3D Li, double dist, double pdf, boolean isDelta) {}
