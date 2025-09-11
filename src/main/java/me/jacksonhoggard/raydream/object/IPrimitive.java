package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.math.Ray;

public interface IPrimitive {
    Hit intersect(Ray ray);
}
