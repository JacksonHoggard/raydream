package me.jacksonhoggard.raydream.math;

public class Matrix3D {

    private double[] matrix;

    public Matrix3D(Matrix3D m) {
        this(
                m.matrix[0], m.matrix[1], m.matrix[2],
                m.matrix[3], m.matrix[4], m.matrix[5],
                m.matrix[6], m.matrix[7], m.matrix[8]
        );
    }

    public Matrix3D(double m00, double m01, double m02,
                    double m10, double m11, double m12,
                    double m20, double m21, double m22) {
        matrix = new double[] {
                m00, m01, m02,
                m10, m11, m12,
                m20, m21, m22
        };
    }

    public Matrix3D(double[] m) {
        this(
                m[0], m[1], m[2],
                m[3], m[4], m[5],
                m[6], m[7], m[8]
        );
    }

    public Matrix3D mult(Matrix3D right) {
        return new Matrix3D(
                matrix[0]*right.matrix[0] + matrix[1]*right.matrix[3] + matrix[2]*right.matrix[6], // c00
                matrix[0]*right.matrix[1] + matrix[1]*right.matrix[4] + matrix[2]*right.matrix[7], // c01
                matrix[0]*right.matrix[2] + matrix[1]*right.matrix[5] + matrix[2]*right.matrix[8], // c02
                matrix[3]*right.matrix[0] + matrix[4]*right.matrix[3] + matrix[5]*right.matrix[6], // c10
                matrix[3]*right.matrix[1] + matrix[4]*right.matrix[4] + matrix[5]*right.matrix[7], // c11
                matrix[3]*right.matrix[2] + matrix[4]*right.matrix[5] + matrix[5]*right.matrix[8], // c12
                matrix[6]*right.matrix[0] + matrix[7]*right.matrix[3] + matrix[8]*right.matrix[6], // c20
                matrix[6]*right.matrix[1] + matrix[7]*right.matrix[4] + matrix[8]*right.matrix[7], // c21
                matrix[6]*right.matrix[2] + matrix[7]*right.matrix[5] + matrix[8]*right.matrix[8]  // c22
        );
    }

    public Matrix3D inverse() {
        double det = matrix[0] * (matrix[4] * matrix[8] - matrix[5] * matrix[7]) -
                     matrix[1] * (matrix[3] * matrix[8] - matrix[5] * matrix[6]) +
                     matrix[2] * (matrix[3] * matrix[7] - matrix[4] * matrix[6]);
        
        if (Math.abs(det) < 1e-10) {
            throw new IllegalStateException("Matrix is not invertible (determinant is zero)");
        }
        
        double invDet = 1.0 / det;
        
        return new Matrix3D(
            (matrix[4] * matrix[8] - matrix[5] * matrix[7]) * invDet,
            (matrix[2] * matrix[7] - matrix[1] * matrix[8]) * invDet,
            (matrix[1] * matrix[5] - matrix[2] * matrix[4]) * invDet,
            (matrix[5] * matrix[6] - matrix[3] * matrix[8]) * invDet,
            (matrix[0] * matrix[8] - matrix[2] * matrix[6]) * invDet,
            (matrix[2] * matrix[3] - matrix[0] * matrix[5]) * invDet,
            (matrix[3] * matrix[7] - matrix[4] * matrix[6]) * invDet,
            (matrix[1] * matrix[6] - matrix[0] * matrix[7]) * invDet,
            (matrix[0] * matrix[4] - matrix[1] * matrix[3]) * invDet
        );
    }

    public Matrix3D transpose() {
        return new Matrix3D(
                matrix[0], matrix[3], matrix[6],
                matrix[1], matrix[4], matrix[7],
                matrix[2], matrix[5], matrix[8]
        );
    }

    public void set(double m00, double m01, double m02,
                    double m10, double m11, double m12,
                    double m20, double m21, double m22) {
        matrix = new double[] {
                m00, m01, m02,
                m10, m11, m12,
                m20, m21, m22
        };
    }

    public void set(double[] m) {
        matrix = new double[] {
            m[0], m[1], m[2],
            m[3], m[4], m[5],
            m[6], m[7], m[8]
        };
    }

    public void set(Matrix3D m) {
        this.set(m.matrix);
    }

    public double get(int i, int j) {
        double[][] temp = new double[][] {
                {matrix[0], matrix[1], matrix[2]},
                {matrix[3], matrix[4], matrix[5]},
                {matrix[6], matrix[7], matrix[8]}
        };
        return temp[i][j];
    }

    public double[] getMatrixArray() {
        return matrix;
    }

    @Override
    public String toString() {
        return  "/ " +  matrix[0] + ", " + matrix[1] + ", " + matrix[2] + " \\\n" +
                "| " +  matrix[3] + ", " + matrix[4] + ", " + matrix[5] + " |\n" +
                "\\ " + matrix[6] + ", " + matrix[7] + ", " + matrix[8] + " /\n";
    }
    
}
