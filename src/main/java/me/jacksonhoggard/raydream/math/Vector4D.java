package me.jacksonhoggard.raydream.math;

public class Vector4D {
    public double x, y, z, w;

    public Vector4D() {
        x = 0;
        y = 0;
        z = 0;
        w = 1;
    }

    public Vector4D(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4D(Vector4D v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.w = v.w;
    }

    public Vector4D set(Vector4D v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.w = v.w;
        return this;
    }

    public Vector4D set(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Vector4D mult(Matrix4D m) {
        return new Vector4D(
                x*m.get(0, 0) + y*m.get(0, 1) + z*m.get(0, 2) + w*m.get(0, 3), // x'
                x*m.get(1, 0) + y*m.get(1, 1) + z*m.get(1, 2) + w*m.get(1, 3), // y'
                x*m.get(2, 0) + y*m.get(2, 1) + z*m.get(2, 2) + w*m.get(2, 3), // z'
                x*m.get(3, 0) + y*m.get(3, 1) + z*m.get(3, 2) + w*m.get(3, 3) // w'
        );
    }

    public Vector4D negate() {
        this.x = -x;
        this.y = -y;
        this.z = -z;
        this.w = -w;
        return this;
    }

    public Vector4D negated() {
        return new Vector4D(this).negate();
    }

    public Vector4D add(Vector4D v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        this.w += v.w;
        return this;
    }

    public Vector4D sub(Vector4D v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        this.w -= v.w;
        return this;
    }
    public Vector4D mult(Vector4D v) {
        this.x *= v.x;
        this.y *= v.y;
        this.z *= v.z;
        this.w *= v.w;
        return this;
    }

    public Vector4D mult(double t) {
        this.x *= t;
        this.y *= t;
        this.z *= t;
        this.w *= t;
        return this;
    }

    public Vector4D div(double t) {
        return this.mult(1/t);
    }

    public static Vector4D add(Vector4D u, Vector4D v) {
        return new Vector4D(u.x + v.x, u.y + v.y, u.z + v.z, u.w + v.w);
    }

    public static Vector4D sub(Vector4D u, Vector4D v) {
        return new Vector4D(u.x - v.x, u.y - v.y, u.z - v.z, u.w - v.w);
    }

    public static Vector4D mult(Vector4D u, Vector4D v) {
        return new Vector4D(u.x * v.x, u.y * v.y, u.z * v.z, u.w * v.w);
    }

    public static Vector4D mult(Vector4D v, double t) {
        return new Vector4D(v).mult(t);
    }

    public static Vector4D mult(double t, Vector4D v) {
        return new Vector4D(v).mult(t);
    }

    public static Vector4D div(Vector4D v, double t) {
        return new Vector4D(v).mult(1/t);
    }

    public double length() {
        return Math.sqrt(x*x + y*y + z*z + w*w);
    }

    public double dot(Vector4D v) {
        return this.x * v.x + this.y * v.y + this.z * v.z + this.w * v.w;
    }

    public Vector4D normalize() {
        return this.div(this.length());
    }

    public Vector4D normalized() {
        return new Vector4D(this).normalize();
    }

    public double distance(Vector4D v) {
        return Vector4D.sub(this, v).length();
    }

    @Override
    public String toString() {
        return "<" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + ">";
    }

    @Override
    public boolean equals(Object obj) {
        Vector4D v = (Vector4D) obj;
        return x == v.x && y == v.y && z == v.z && w == v.w;
    }
}
