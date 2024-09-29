package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public record Hit(Object object, Vector3D point, Vector3D normal, Vector2D texCoord, double t) {

}
