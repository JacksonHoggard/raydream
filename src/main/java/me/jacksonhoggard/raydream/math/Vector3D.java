package me.jacksonhoggard.raydream.math;

public class Vector3D {

    public double x, y, z;

    public Vector3D() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(Vector3D v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public Vector3D set(Vector3D v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        return this;
    }

    public Vector3D set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3D negate() {
        this.x = -x;
        this.y = -y;
        this.z = -z;
        return this;
    }

    public Vector3D negated() {
        return new Vector3D(this).negate();
    }

    public Vector3D add(Vector3D v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return this;
    }

    public Vector3D sub(Vector3D v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        return this;
    }
    public Vector3D mult(Vector3D v) {
        this.x *= v.x;
        this.y *= v.y;
        this.z *= v.z;
        return this;
    }

    public Vector3D mult(double t) {
        this.x *= t;
        this.y *= t;
        this.z *= t;
        return this;
    }

    public Vector3D div(double t) {
        return this.mult(1/t);
    }

    public static Vector3D add(Vector3D u, Vector3D v) {
        return new Vector3D(u.x + v.x, u.y + v.y, u.z + v.z);
    }

    public static Vector3D sub(Vector3D u, Vector3D v) {
        return new Vector3D(u.x - v.x, u.y - v.y, u.z - v.z);
    }

    public static Vector3D mult(Vector3D u, Vector3D v) {
        return new Vector3D(u.x * v.x, u.y * v.y, u.z * v.z);
    }

    public static Vector3D mult(Vector3D v, double t) {
        return new Vector3D(v).mult(t);
    }

    public static Vector3D mult(double t, Vector3D v) {
        return new Vector3D(v).mult(t);
    }

    public static Vector3D div(Vector3D v, double t) {
        return new Vector3D(v).mult(1/t);
    }

    public double length() {
        return Math.sqrt(x*x + y*y + z*z);
    }

    public double dot(Vector3D v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public Vector3D cross(Vector3D v) {
        return new Vector3D(
                this.y * v.z - this.z * v.y,
                this.z * v.x - this.x * v.z,
                this.x * v.y - this.y * v.x
        );
    }

    public Vector3D normalize() {
        return this.div(this.length());
    }

    public Vector3D normalized() {
        return new Vector3D(this).normalize();
    }

    public double distance(Vector3D v) {
        return Vector3D.sub(this, v).length();
    }

    public double[] toArray() {
        return new double[] {x, y, z};
    }

    @Override
    public String toString() {
        return "<" + this.x + ", " + this.y + ", " + this.z + ">";
    }

    @Override
    public boolean equals(Object obj) {
        Vector3D v = (Vector3D) obj;
        return x == v.x && y == v.y && z == v.z;
    }
}