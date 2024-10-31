package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.math.Vector3D;

public record Mesh(String path, Triangle[] triangles, Vector3D min, Vector3D max) {

}
