package me.jacksonhoggard.raydream.math;

public class Frame {

    public final Vector3D x, y, z;

    public Frame() {
        this.x = new Vector3D(1, 0, 0);
        this.y = new Vector3D(0, 1, 0);
        this.z = new Vector3D(0, 0, 1);
    }

    public Frame(Vector3D x, Vector3D y, Vector3D z) {
        this.x = new Vector3D(x);
        this.y = new Vector3D(y);
        this.z = new Vector3D(z);
    }

    public static Frame fromXZ(Vector3D x, Vector3D z) {
        return new Frame(x, z.cross(x), z);
    }

    public static Frame fromXY(Vector3D x, Vector3D y) {
        return new Frame(x, y, x.cross(y));
    }

    public static Frame fromZ(Vector3D z) {
        Vector3D x, y;
        coordinateSystem(z, x = new Vector3D(), y = new Vector3D());
        return new Frame(x, y, z);
    }

    public static Frame fromY(Vector3D y) {
        Vector3D x, z;
        coordinateSystem(y, z = new Vector3D(), x = new Vector3D());
        return new Frame(x, y, z);
    }

    public static Frame fromX(Vector3D x) {
        Vector3D y, z;
        coordinateSystem(x, y = new Vector3D(), z = new Vector3D());
        return new Frame(x, y, z);
    }

    public Vector3D toLocal(Vector3D v) {
        return new Vector3D(
            v.dot(x),
            v.dot(y),
            v.dot(z)
        );
    }

    public Vector3D toWorld(Vector3D v) {
        return Vector3D.add(
            Vector3D.add(
                Vector3D.mult(x, v.x),
                Vector3D.mult(y, v.y)
            ),
            Vector3D.mult(z, v.z)
        );
    }

    private static void coordinateSystem(Vector3D  v1, Vector3D v2, Vector3D v3) {
        double sign = Math.copySign(1.0, v1.z);
        double a = -1.0 / (sign + v1.z);
        double b = v1.x * v1.y * a;
        v2.set(1.0 + sign * (v1.x * v1.x) * a, sign * b, -sign * v1.x);
        v3.set(b, sign + (v1.y * v1.y) * a, -v1.y);
    }

    @Override
    public String toString() {
        return "Frame{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
