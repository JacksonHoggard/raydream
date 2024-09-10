package me.jacksonhoggard.raydream.math;

public class Vector4F {
    public float x, y, z, w;

    public Vector4F() {
        x = 0;
        y = 0;
        z = 0;
        w = 1;
    }

    public Vector4F(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vector4F(Vector4F v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.w = v.w;
    }

    public Vector4F set(Vector4F v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.w = v.w;
        return this;
    }

    public Vector4F set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Vector4F mult(Matrix4F m) {
        return new Vector4F(
                x*m.get(0, 0) + y*m.get(0, 1) + z*m.get(0, 2) + w*m.get(0, 3), // x'
                x*m.get(1, 0) + y*m.get(1, 1) + z*m.get(1, 2) + w*m.get(1, 3), // y'
                x*m.get(2, 0) + y*m.get(2, 1) + z*m.get(2, 2) + w*m.get(2, 3), // z'
                x*m.get(3, 0) + y*m.get(3, 1) + z*m.get(3, 2) + w*m.get(3, 3) // w'
        );
    }

    public Vector4F negate() {
        this.x = -x;
        this.y = -y;
        this.z = -z;
        this.w = -w;
        return this;
    }

    public Vector4F negated() {
        return new Vector4F(this).negate();
    }

    public Vector4F add(Vector4F v) {
        this.x += v.x;
        this.y += v.y;
        this.z += v.z;
        this.w += v.w;
        return this;
    }

    public Vector4F sub(Vector4F v) {
        this.x -= v.x;
        this.y -= v.y;
        this.z -= v.z;
        this.w -= v.w;
        return this;
    }
    public Vector4F mult(Vector4F v) {
        this.x *= v.x;
        this.y *= v.y;
        this.z *= v.z;
        this.w *= v.w;
        return this;
    }

    public Vector4F mult(float t) {
        this.x *= t;
        this.y *= t;
        this.z *= t;
        this.w *= t;
        return this;
    }

    public Vector4F div(float t) {
        return this.mult(1/t);
    }

    public static Vector4F add(Vector4F u, Vector4F v) {
        return new Vector4F(u.x + v.x, u.y + v.y, u.z + v.z, u.w + v.w);
    }

    public static Vector4F sub(Vector4F u, Vector4F v) {
        return new Vector4F(u.x - v.x, u.y - v.y, u.z - v.z, u.w - v.w);
    }

    public static Vector4F mult(Vector4F u, Vector4F v) {
        return new Vector4F(u.x * v.x, u.y * v.y, u.z * v.z, u.w * v.w);
    }

    public static Vector4F mult(Vector4F v, float t) {
        return new Vector4F(v).mult(t);
    }

    public static Vector4F mult(float t, Vector4F v) {
        return new Vector4F(v).mult(t);
    }

    public static Vector4F div(Vector4F v, float t) {
        return new Vector4F(v).mult(1/t);
    }

    public float length() {
        return (float) Math.sqrt(x*x + y*y + z*z + w*w);
    }

    public float dot(Vector4F v) {
        return this.x * v.x + this.y * v.y + this.z * v.z + this.w * v.w;
    }

    public Vector4F normalize() {
        return this.div(this.length());
    }

    public Vector4F normalized() {
        return new Vector4F(this).normalize();
    }

    public float distance(Vector4F v) {
        return Vector4F.sub(this, v).length();
    }

    @Override
    public String toString() {
        return "<" + this.x + ", " + this.y + ", " + this.z + ", " + this.w + ">";
    }

    @Override
    public boolean equals(Object obj) {
        Vector4F v = (Vector4F) obj;
        return x == v.x && y == v.y && z == v.z && w == v.w;
    }
}
