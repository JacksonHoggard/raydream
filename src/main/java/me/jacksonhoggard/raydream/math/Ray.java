package me.jacksonhoggard.raydream.math;

public record Ray(Vector3D origin, Vector3D direction) {

    public Vector3D at(double t) {
        return Vector3D.add(origin, Vector3D.mult(direction, t));
    }
}
