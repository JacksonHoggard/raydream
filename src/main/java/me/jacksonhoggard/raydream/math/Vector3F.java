package me.jacksonhoggard.raydream.math;

public class Vector3F {

    public float x, y, z;

    public Vector3F() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vector3F(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3F(float t) {
        this.x = t;
        this.y = t;
        this.z = t;
    }

    public Vector3F(Vector3F v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public Vector3F set(Vector3F v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        return this;
    }

    public Vector3F set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vector3F negate() {
        this.x = -x;
        this.y = -y;
        this.z = -z;
        return this;
    }

    public Vector3F negated() {
        return new Vector3F(this).negate();
    }

    public Vector3F add(Vector3F v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        return this;
    }

    public Vector3F sub(Vector3F v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        return this;
    }
    public Vector3F mult(Vector3F v) {
        this.x *= v.x;
        this.y *= v.y;
        this.z *= v.z;
        return this;
    }

    public Vector3F mult(float t) {
        this.x *= t;
        this.y *= t;
        this.z *= t;
        return this;
    }

    public Vector3F div(float t) {
        return this.mult(1/t);
    }

    public static Vector3F add(Vector3F u, Vector3F v) {
        return new Vector3F(u.x + v.x, u.y + v.y, u.z + v.z);
    }

    public static Vector3F sub(Vector3F u, Vector3F v) {
        return new Vector3F(u.x - v.x, u.y - v.y, u.z - v.z);
    }

    public static Vector3F mult(Vector3F u, Vector3F v) {
        return new Vector3F(u.x * v.x, u.y * v.y, u.z * v.z);
    }

    public static Vector3F mult(Vector3F v, float t) {
        return new Vector3F(v).mult(t);
    }

    public static Vector3F mult(float t, Vector3F v) {
        return new Vector3F(v).mult(t);
    }

    public static Vector3F div(Vector3F v, float t) {
        return new Vector3F(v).mult(1/t);
    }

    public float length() {
        return (float) Math.sqrt(x*x + y*y + z*z);
    }

    public float dot(Vector3F v) {
        return this.x * v.x + this.y * v.y + this.z * v.z;
    }

    public Vector3F cross(Vector3F v) {
        return new Vector3F(
                this.y * v.z - this.z * v.y,
                this.z * v.x - this.x * v.z,
                this.x * v.y - this.y * v.x
        );
    }

    public Vector3F normalize() {
        return this.div(this.length());
    }

    public Vector3F normalized() {
        return new Vector3F(this).normalize();
    }

    public float distance(Vector3F v) {
        return Vector3F.sub(this, v).length();
    }

    public float[] toArray() {
        return new float[]{x, y, z};
    }

    @Override
    public String toString() {
        return "<" + this.x + ", " + this.y + ", " + this.z + ">";
    }

    @Override
    public boolean equals(Object obj) {
        Vector3F v = (Vector3F) obj;
        return x == v.x && y == v.y && z == v.z;
    }
}