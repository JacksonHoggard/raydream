package me.jacksonhoggard.raydream.math;

public class Vector2F {

    public float x, y;

    public Vector2F() {
        this.x = 0;
        this.y = 0;
    }

    public Vector2F(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2F(float t) {
        this.x = t;
        this.y = t;
    }

    public Vector2F(Vector2F v) {
        this.x = v.x;
        this.y = v.y;
    }

    public Vector2F set(Vector2F v) {
        this.x = v.x;
        this.y = v.y;
        return this;
    }

    public Vector2F set(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2F negate() {
        this.x = -x;
        this.y = -y;
        return this;
    }

    public Vector2F negated() {
        return new Vector2F(this).negate();
    }

    public Vector2F add(Vector2F v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    public Vector2F sub(Vector2F v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    public Vector2F mult(Vector2F v) {
        this.x *= v.x;
        this.y *= v.y;
        return this;
    }

    public Vector2F mult(float t) {
        this.x *= t;
        this.y *= t;
        return this;
    }

    public Vector2F div(float t) {
        return this.mult(1/t);
    }

    public static Vector2F add(Vector2F u, Vector2F v) {
        return new Vector2F(u).add(v);
    }

    public static Vector2F sub(Vector2F u, Vector2F v) {
        return new Vector2F(u).sub(v);
    }

    public static Vector2F mult(Vector2F u, Vector2F v) {
        return new Vector2F(u).mult(v);
    }

    public static Vector2F mult(Vector2F u, float t) {
        return new Vector2F(u).mult(t);
    }

    public static Vector2F mult(float t, Vector2F u) {
        return mult(u, t);
    }

    public static Vector2F div(Vector2F u, float t) {
        return new Vector2F(u).div(t);
    }

    public float length() {
        return (float) Math.sqrt(x*x + y*y);
    }

    public float dot(Vector2F v) {
        return this.x * v.x + this.y * v.y;
    }

    public float cross(Vector2F v) {
        return x*v.y - v.x*y;
    }

    public Vector2F normalize() {
        return this.div(this.length());
    }

    public Vector2F normalized() {
        return new Vector2F(this).normalize();
    }

    public float distance(Vector2F v) {
        return Vector2F.sub(this, v).length();
    }

    public float[] toArray() {
        return new float[]{x, y};
    }

    @Override
    public String toString() {
        return "<" + this.x + ", " + this.y + ">";
    }

    @Override
    public boolean equals(Object obj) {
        Vector2F v = (Vector2F) obj;
        return x == v.x && y == v.y;
    }
}
