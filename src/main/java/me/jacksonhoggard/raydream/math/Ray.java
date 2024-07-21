package me.jacksonhoggard.raydream.math;

public class Ray {

    private final Vector3D origin;
    private final Vector3D direction;

    public Ray(Vector3D origin, Vector3D direction) {
        this.origin = origin;
        this.direction = direction;
    }

    public Vector3D at(double t) {
        return Vector3D.add(origin, Vector3D.mult(direction, t));
    }

    public Vector3D getOrigin() {
        return origin;
    }

    public Vector3D getDirection() {
        return direction;
    }
}
