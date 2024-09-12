package me.jacksonhoggard.raydream.math;

public class Matrix4D {
    private double[] matrix;

    public Matrix4D(Matrix4D m) {
        this(
                m.matrix[0], m.matrix[1], m.matrix[2], m.matrix[3],
                m.matrix[4], m.matrix[5], m.matrix[6], m.matrix[7],
                m.matrix[8], m.matrix[9], m.matrix[10], m.matrix[11],
                m.matrix[12], m.matrix[13], m.matrix[14], m.matrix[15]
        );
    }

    public Matrix4D(double m00, double m01, double m02, double m03,
                    double m10, double m11, double m12, double m13,
                    double m20, double m21, double m22, double m23,
                    double m30, double m31, double m32, double m33) {
        matrix = new double[] {
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
        };
    }

    public Matrix4D(double[] m) {
        this(
                m[0], m[1], m[2], m[3],
                m[4], m[5], m[6], m[7],
                m[8], m[9], m[10], m[11],
                m[12], m[13], m[14], m[15]
        );
    }

    public Matrix4D mult(Matrix4D right) {
        return new Matrix4D(
                matrix[0]*right.matrix[0] + matrix[1]*right.matrix[4] + matrix[2]*right.matrix[8] + matrix[3]*right.matrix[12], // c00
                matrix[0]*right.matrix[1] + matrix[1]*right.matrix[5] + matrix[2]*right.matrix[9] +  matrix[3]*right.matrix[13], // c01
                matrix[0]*right.matrix[2] + matrix[1]*right.matrix[6] + matrix[2]*right.matrix[10] + matrix[3]*right.matrix[14], // c02
                matrix[0]*right.matrix[3] + matrix[1]*right.matrix[7] + matrix[2]*right.matrix[11] + matrix[3]*right.matrix[15], // c03

                matrix[4]*right.matrix[0] + matrix[5]*right.matrix[4] + matrix[6]*right.matrix[8]  + matrix[7]*right.matrix[12], // c10
                matrix[4]*right.matrix[1] + matrix[5]*right.matrix[5] + matrix[6]*right.matrix[9]  + matrix[7]*right.matrix[13], // c11
                matrix[4]*right.matrix[2] + matrix[5]*right.matrix[6] + matrix[6]*right.matrix[10] + matrix[7]*right.matrix[14], // c12
                matrix[4]*right.matrix[3] + matrix[5]*right.matrix[7] + matrix[6]*right.matrix[11] + matrix[7]*right.matrix[15], // c13

                matrix[8]*right.matrix[0] + matrix[9]*right.matrix[4] + matrix[10]*right.matrix[8]  + matrix[11]*right.matrix[12], // c20
                matrix[8]*right.matrix[1] + matrix[9]*right.matrix[5] + matrix[10]*right.matrix[9]  + matrix[11]*right.matrix[13], // c21
                matrix[8]*right.matrix[2] + matrix[9]*right.matrix[6] + matrix[10]*right.matrix[10] + matrix[11]*right.matrix[14], // c22
                matrix[8]*right.matrix[3] + matrix[9]*right.matrix[7] + matrix[10]*right.matrix[11] + matrix[11]*right.matrix[15], // c23

                matrix[12]*right.matrix[0] + matrix[13]*right.matrix[4] + matrix[14]*right.matrix[8]  + matrix[15]*right.matrix[12], // c30
                matrix[12]*right.matrix[1] + matrix[13]*right.matrix[5] + matrix[14]*right.matrix[9]  + matrix[15]*right.matrix[13], // c31
                matrix[12]*right.matrix[2] + matrix[13]*right.matrix[6] + matrix[14]*right.matrix[10] + matrix[15]*right.matrix[14], // c32
                matrix[12]*right.matrix[3] + matrix[13]*right.matrix[7] + matrix[14]*right.matrix[11] + matrix[15]*right.matrix[15]  // c33
        );
    }

    public Matrix4D inverse() {
        double[] inv = new double[16];

        inv[0] = matrix[5]  * matrix[10] * matrix[15] -
                matrix[5]  * matrix[11] * matrix[14] -
                matrix[9]  * matrix[6]  * matrix[15] +
                matrix[9]  * matrix[7]  * matrix[14] +
                matrix[13] * matrix[6]  * matrix[11] -
                matrix[13] * matrix[7]  * matrix[10];

        inv[4] = -matrix[4]  * matrix[10] * matrix[15] +
                matrix[4]  * matrix[11] * matrix[14] +
                matrix[8]  * matrix[6]  * matrix[15] -
                matrix[8]  * matrix[7]  * matrix[14] -
                matrix[12] * matrix[6]  * matrix[11] +
                matrix[12] * matrix[7]  * matrix[10];

        inv[8] = matrix[4]  * matrix[9] * matrix[15] -
                matrix[4]  * matrix[11] * matrix[13] -
                matrix[8]  * matrix[5] * matrix[15] +
                matrix[8]  * matrix[7] * matrix[13] +
                matrix[12] * matrix[5] * matrix[11] -
                matrix[12] * matrix[7] * matrix[9];

        inv[12] = -matrix[4]  * matrix[9] * matrix[14] +
                matrix[4]  * matrix[10] * matrix[13] +
                matrix[8]  * matrix[5] * matrix[14] -
                matrix[8]  * matrix[6] * matrix[13] -
                matrix[12] * matrix[5] * matrix[10] +
                matrix[12] * matrix[6] * matrix[9];

        inv[1] = -matrix[1]  * matrix[10] * matrix[15] +
                matrix[1]  * matrix[11] * matrix[14] +
                matrix[9]  * matrix[2] * matrix[15] -
                matrix[9]  * matrix[3] * matrix[14] -
                matrix[13] * matrix[2] * matrix[11] +
                matrix[13] * matrix[3] * matrix[10];

        inv[5] = matrix[0]  * matrix[10] * matrix[15] -
                matrix[0]  * matrix[11] * matrix[14] -
                matrix[8]  * matrix[2] * matrix[15] +
                matrix[8]  * matrix[3] * matrix[14] +
                matrix[12] * matrix[2] * matrix[11] -
                matrix[12] * matrix[3] * matrix[10];

        inv[9] = -matrix[0]  * matrix[9] * matrix[15] +
                matrix[0]  * matrix[11] * matrix[13] +
                matrix[8]  * matrix[1] * matrix[15] -
                matrix[8]  * matrix[3] * matrix[13] -
                matrix[12] * matrix[1] * matrix[11] +
                matrix[12] * matrix[3] * matrix[9];

        inv[13] = matrix[0]  * matrix[9] * matrix[14] -
                matrix[0]  * matrix[10] * matrix[13] -
                matrix[8]  * matrix[1] * matrix[14] +
                matrix[8]  * matrix[2] * matrix[13] +
                matrix[12] * matrix[1] * matrix[10] -
                matrix[12] * matrix[2] * matrix[9];

        inv[2] = matrix[1]  * matrix[6] * matrix[15] -
                matrix[1]  * matrix[7] * matrix[14] -
                matrix[5]  * matrix[2] * matrix[15] +
                matrix[5]  * matrix[3] * matrix[14] +
                matrix[13] * matrix[2] * matrix[7] -
                matrix[13] * matrix[3] * matrix[6];

        inv[6] = -matrix[0]  * matrix[6] * matrix[15] +
                matrix[0]  * matrix[7] * matrix[14] +
                matrix[4]  * matrix[2] * matrix[15] -
                matrix[4]  * matrix[3] * matrix[14] -
                matrix[12] * matrix[2] * matrix[7] +
                matrix[12] * matrix[3] * matrix[6];

        inv[10] = matrix[0]  * matrix[5] * matrix[15] -
                matrix[0]  * matrix[7] * matrix[13] -
                matrix[4]  * matrix[1] * matrix[15] +
                matrix[4]  * matrix[3] * matrix[13] +
                matrix[12] * matrix[1] * matrix[7] -
                matrix[12] * matrix[3] * matrix[5];

        inv[14] = -matrix[0]  * matrix[5] * matrix[14] +
                matrix[0]  * matrix[6] * matrix[13] +
                matrix[4]  * matrix[1] * matrix[14] -
                matrix[4]  * matrix[2] * matrix[13] -
                matrix[12] * matrix[1] * matrix[6] +
                matrix[12] * matrix[2] * matrix[5];

        inv[3] = -matrix[1] * matrix[6] * matrix[11] +
                matrix[1] * matrix[7] * matrix[10] +
                matrix[5] * matrix[2] * matrix[11] -
                matrix[5] * matrix[3] * matrix[10] -
                matrix[9] * matrix[2] * matrix[7] +
                matrix[9] * matrix[3] * matrix[6];

        inv[7] = matrix[0] * matrix[6] * matrix[11] -
                matrix[0] * matrix[7] * matrix[10] -
                matrix[4] * matrix[2] * matrix[11] +
                matrix[4] * matrix[3] * matrix[10] +
                matrix[8] * matrix[2] * matrix[7] -
                matrix[8] * matrix[3] * matrix[6];

        inv[11] = -matrix[0] * matrix[5] * matrix[11] +
                matrix[0] * matrix[7] * matrix[9] +
                matrix[4] * matrix[1] * matrix[11] -
                matrix[4] * matrix[3] * matrix[9] -
                matrix[8] * matrix[1] * matrix[7] +
                matrix[8] * matrix[3] * matrix[5];

        inv[15] = matrix[0] * matrix[5] * matrix[10] -
                matrix[0] * matrix[6] * matrix[9] -
                matrix[4] * matrix[1] * matrix[10] +
                matrix[4] * matrix[2] * matrix[9] +
                matrix[8] * matrix[1] * matrix[6] -
                matrix[8] * matrix[2] * matrix[5];
        double det = matrix[0] * inv[0] + matrix[1] * inv[4] + matrix[2] * inv[8] + matrix[3] * inv[12];
        if(det == 0)
            return new Matrix4D(this);

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
                matrix[0], matrix[4], matrix[8], matrix[12],
                matrix[1], matrix[5], matrix[9], matrix[13],
                matrix[2], matrix[6], matrix[10], matrix[14],
                matrix[3], matrix[7], matrix[11], matrix[15]
        );
    }

    public void set(double m00, double m01, double m02, double m03,
                    double m10, double m11, double m12, double m13,
                    double m20, double m21, double m22, double m23,
                    double m30, double m31, double m32, double m33) {
        matrix = new double[] {
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
        };
    }

    public void set(double[] m) {
        matrix = new double[] {
                m[0], m[1], m[2], m[3],
                m[4], m[5], m[6], m[7],
                m[8], m[9], m[10], m[11],
                m[12], m[13], m[14], m[15]
        };
    }

    public void set(Matrix4D m) {
        this.set(m.matrix);
    }

    public double get(int i, int j) {
        double[][] temp = new double[][] {
                {matrix[0], matrix[1], matrix[2], matrix[3]},
                {matrix[4], matrix[5], matrix[6], matrix[7]},
                {matrix[8], matrix[9], matrix[10], matrix[11]},
                {matrix[12], matrix[13], matrix[14], matrix[15]}
        };
        return temp[i][j];
    }

    public double[] getMatrixArray() {
        return matrix;
    }

    @Override
    public String toString() {
        return "/ " + matrix[0] + ", " + matrix[1] + ", " + matrix[2] + ", " + matrix[3] + " \\\n" +
                "| " + matrix[4] + ", " + matrix[5] + ", " + matrix[6] + ", " + matrix[7] + " |\n" +
                "| " + matrix[8] + ", " + matrix[9] + ", " + matrix[10] + ", " + matrix[11] + " |\n" +
                "\\ " + matrix[12] + ", " + matrix[13] + ", " + matrix[14] + ", " + matrix[15] + " /\n";
    }
}
