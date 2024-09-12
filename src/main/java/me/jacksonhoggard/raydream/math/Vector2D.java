package me.jacksonhoggard.raydream.math;

public class Vector2D {

    public double x, y;

    public Vector2D() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D(Vector2D v) {
        this.x = v.x;
        this.y = v.y;
    }

    public Vector2D set(Vector2D v) {
        this.x = v.x;
        this.y = v.y;
        return this;
    }

    public Vector2D set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2D negate() {
        this.x = -x;
        this.y = -y;
        return this;
    }

    public Vector2D negated() {
        return new Vector2D(this).negate();
    }

    public Vector2D add(Vector2D v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    public Vector2D sub(Vector2D v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    public Vector2D mult(Vector2D v) {
        this.x *= v.x;
        this.y *= v.y;
        return this;
    }

    public Vector2D mult(double t) {
        this.x *= t;
        this.y *= t;
        return this;
    }

    public Vector2D div(double t) {
        return this.mult(1/t);
    }

    public static Vector2D add(Vector2D u, Vector2D v) {
        return new Vector2D(u).add(v);
    }

    public static Vector2D sub(Vector2D u, Vector2D v) {
        return new Vector2D(u).sub(v);
    }

    public static Vector2D mult(Vector2D u, Vector2D v) {
        return new Vector2D(u).mult(v);
    }

    public static Vector2D mult(Vector2D u, double t) {
        return new Vector2D(u).mult(t);
    }

    public static Vector2D mult(double t, Vector2D u) {
        return mult(u, t);
    }

    public static Vector2D div(Vector2D u, double t) {
        return new Vector2D(u).div(t);
    }

    public double length() {
        return Math.sqrt(x*x + y*y);
    }

    public double dot(Vector2D v) {
        return this.x * v.x + this.y * v.y;
    }

    public double cross(Vector2D v) {
        return x*v.y - v.x*y;
    }

    public Vector2D normalize() {
        return this.div(this.length());
    }

    public Vector2D normalized() {
        return new Vector2D(this).normalize();
    }

    public double distance(Vector2D v) {
        return Vector2D.sub(this, v).length();
    }

    public double[] toArray() {
        return new double[]{x, y};
    }

    @Override
    public String toString() {
        return "<" + this.x + ", " + this.y + ">";
    }

    @Override
    public boolean equals(Object obj) {
        Vector2D v = (Vector2D) obj;
        return x == v.x && y == v.y;
    }
}
