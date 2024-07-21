package me.jacksonhoggard.raydream.math;

public class Matrix4D {
    private double[][] matrix;
    private double m00, m01, m02, m03;
    private double m10, m11, m12, m13;
    private double m20, m21, m22, m23;
    private double m30, m31, m32, m33;

    public Matrix4D(double m00, double m01, double m02, double m03,
                    double m10, double m11, double m12, double m13,
                    double m20, double m21, double m22, double m23,
                    double m30, double m31, double m32, double m33) {
        matrix = new double[][] {
                {m00, m01, m02, m03},
                {m10, m11, m12, m13},
                {m20, m21, m22, m23},
                {m30, m31, m32, m33}
        };
        this.m00 = m00;
        this.m01 = m01;
        this.m02 = m02;
        this.m03 = m03;
        this.m10 = m10;
        this.m11 = m11;
        this.m12 = m12;
        this.m13 = m13;
        this.m20 = m20;
        this.m21 = m21;
        this.m22 = m22;
        this.m23 = m23;
        this.m30 = m30;
        this.m31 = m31;
        this.m32 = m32;
        this.m33 = m33;
    }

    public Matrix4D(Matrix4D m) {
        this.matrix = m.matrix;
    }

    public Matrix4D mult(Matrix4D right) {
        return new Matrix4D(
                m00*right.m00 + m01*right.m10 + m02*right.m20 + m03*right.m30, // c00
                m00*right.m01 + m01*right.m11 + m02*right.m21 + m03*right.m31, // c01
                m00*right.m02 + m01*right.m12 + m02*right.m22 + m03*right.m32, // c02
                m00*right.m03 + m01*right.m13 + m02*right.m23 + m03*right.m33, // c03

                m10*right.m00 + m11*right.m10 + m12*right.m20 + m13*right.m30, // c10
                m10*right.m01 + m11*right.m11 + m12*right.m21 + m13*right.m31, // c11
                m10*right.m02 + m11*right.m12 + m12*right.m22 + m13*right.m32, // c12
                m10*right.m03 + m11*right.m13 + m12*right.m23 + m13*right.m33, // c13

                m20*right.m00 + m21*right.m10 + m22*right.m20 + m23*right.m30, // c20
                m20*right.m01 + m21*right.m11 + m22*right.m21 + m23*right.m31, // c21
                m20*right.m02 + m21*right.m12 + m22*right.m22 + m23*right.m32, // c22
                m20*right.m03 + m21*right.m13 + m22*right.m23 + m23*right.m33, // c23

                m30*right.m00 + m31*right.m10 + m32*right.m20 + m33*right.m30, // c30
                m30*right.m01 + m31*right.m11 + m32*right.m21 + m33*right.m31, // c31
                m30*right.m02 + m31*right.m12 + m32*right.m22 + m33*right.m32, // c32
                m30*right.m03 + m31*right.m13 + m32*right.m23 + m33*right.m33  // c33
        );
    }

    public Matrix4D inverse() {
        double[] m = new double[16];
        double[] inv = new double[16];
        int x = 0, y = 0;
        for(int i = 0; i < 16; i++) {
            if(y >= 4) {
                y = 0;
                x++;
            }
            m[i] = get(x, y);
            y++;
        }

        inv[0] = m[5]  * m[10] * m[15] -
                m[5]  * m[11] * m[14] -
                m[9]  * m[6]  * m[15] +
                m[9]  * m[7]  * m[14] +
                m[13] * m[6]  * m[11] -
                m[13] * m[7]  * m[10];

        inv[4] = -m[4]  * m[10] * m[15] +
                m[4]  * m[11] * m[14] +
                m[8]  * m[6]  * m[15] -
                m[8]  * m[7]  * m[14] -
                m[12] * m[6]  * m[11] +
                m[12] * m[7]  * m[10];

        inv[8] = m[4]  * m[9] * m[15] -
                m[4]  * m[11] * m[13] -
                m[8]  * m[5] * m[15] +
                m[8]  * m[7] * m[13] +
                m[12] * m[5] * m[11] -
                m[12] * m[7] * m[9];

        inv[12] = -m[4]  * m[9] * m[14] +
                m[4]  * m[10] * m[13] +
                m[8]  * m[5] * m[14] -
                m[8]  * m[6] * m[13] -
                m[12] * m[5] * m[10] +
                m[12] * m[6] * m[9];

        inv[1] = -m[1]  * m[10] * m[15] +
                m[1]  * m[11] * m[14] +
                m[9]  * m[2] * m[15] -
                m[9]  * m[3] * m[14] -
                m[13] * m[2] * m[11] +
                m[13] * m[3] * m[10];

        inv[5] = m[0]  * m[10] * m[15] -
                m[0]  * m[11] * m[14] -
                m[8]  * m[2] * m[15] +
                m[8]  * m[3] * m[14] +
                m[12] * m[2] * m[11] -
                m[12] * m[3] * m[10];

        inv[9] = -m[0]  * m[9] * m[15] +
                m[0]  * m[11] * m[13] +
                m[8]  * m[1] * m[15] -
                m[8]  * m[3] * m[13] -
                m[12] * m[1] * m[11] +
                m[12] * m[3] * m[9];

        inv[13] = m[0]  * m[9] * m[14] -
                m[0]  * m[10] * m[13] -
                m[8]  * m[1] * m[14] +
                m[8]  * m[2] * m[13] +
                m[12] * m[1] * m[10] -
                m[12] * m[2] * m[9];

        inv[2] = m[1]  * m[6] * m[15] -
                m[1]  * m[7] * m[14] -
                m[5]  * m[2] * m[15] +
                m[5]  * m[3] * m[14] +
                m[13] * m[2] * m[7] -
                m[13] * m[3] * m[6];

        inv[6] = -m[0]  * m[6] * m[15] +
                m[0]  * m[7] * m[14] +
                m[4]  * m[2] * m[15] -
                m[4]  * m[3] * m[14] -
                m[12] * m[2] * m[7] +
                m[12] * m[3] * m[6];

        inv[10] = m[0]  * m[5] * m[15] -
                m[0]  * m[7] * m[13] -
                m[4]  * m[1] * m[15] +
                m[4]  * m[3] * m[13] +
                m[12] * m[1] * m[7] -
                m[12] * m[3] * m[5];

        inv[14] = -m[0]  * m[5] * m[14] +
                m[0]  * m[6] * m[13] +
                m[4]  * m[1] * m[14] -
                m[4]  * m[2] * m[13] -
                m[12] * m[1] * m[6] +
                m[12] * m[2] * m[5];

        inv[3] = -m[1] * m[6] * m[11] +
                m[1] * m[7] * m[10] +
                m[5] * m[2] * m[11] -
                m[5] * m[3] * m[10] -
                m[9] * m[2] * m[7] +
                m[9] * m[3] * m[6];

        inv[7] = m[0] * m[6] * m[11] -
                m[0] * m[7] * m[10] -
                m[4] * m[2] * m[11] +
                m[4] * m[3] * m[10] +
                m[8] * m[2] * m[7] -
                m[8] * m[3] * m[6];

        inv[11] = -m[0] * m[5] * m[11] +
                m[0] * m[7] * m[9] +
                m[4] * m[1] * m[11] -
                m[4] * m[3] * m[9] -
                m[8] * m[1] * m[7] +
                m[8] * m[3] * m[5];

        inv[15] = m[0] * m[5] * m[10] -
                m[0] * m[6] * m[9] -
                m[4] * m[1] * m[10] +
                m[4] * m[2] * m[9] +
                m[8] * m[1] * m[6] -
                m[8] * m[2] * m[5];
        double det = m[0] * inv[0] + m[1] * inv[4] + m[2] * inv[8] + m[3] * inv[12];
        if(det == 0)
            return this;

        det = 1.0D / det;

        return new Matrix4D(
                inv[0] * det, inv[1] * det, inv[2] * det, inv[3] * det,
                inv[4] * det, inv[5] * det, inv[6] * det, inv[7] * det,
                inv[8] * det, inv[9] * det, inv[10] * det, inv[11] * det,
                inv[12] * det, inv[13] * det, inv[14] * det, inv[15] * det
        );
    }

    public Matrix4D transpose() {
        return new Matrix4D(
                m00, m10, m20, m30,
                m01, m11, m21, m31,
                m02, m12, m22, m32,
                m03, m13, m23, m33
        );
    }

    public double get(int i, int j) {
        return matrix[i][j];
    }
}
