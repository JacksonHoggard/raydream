package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.math.Vector3D;

public class Mesh {

    private final Triangle[] triangles;
    private final Vector3D min;
    private final Vector3D max;

    public Mesh(Triangle[] triangles, Vector3D min, Vector3D max) {
        this.triangles = triangles;
        this.min = min;
        this.max = max;
    }

    public Triangle[] getTriangles() {
        return triangles;
    }

    public Vector3D getMin() {
        return min;
    }

    public Vector3D getMax() {
        return max;
    }
}
