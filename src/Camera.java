import math.Ray;
import math.Vector3D;
import util.Util;

public class Camera {

    private final Vector3D lookFrom;
    private final Vector3D u, v, w;
    private static final Vector3D vUp = new Vector3D(0, 1, 0);
    private final double focalLength;
    private final double vHeight;
    private final double vWidth;
    private final int imgWidth;
    private final int imgHeight;

    public Camera(Vector3D lookFrom, Vector3D lookAt, double fov, int imgWidth, int imgHeight) {
        this.lookFrom = lookFrom;
        this.focalLength = Vector3D.sub(lookFrom, lookAt).length();
        this.imgHeight = imgHeight;
        this.imgWidth = imgWidth;
        vHeight = 2 * Math.tan(Math.toRadians(fov) / 2) * focalLength;
        vWidth = vHeight * (imgWidth / (double) imgHeight);
        w = Vector3D.sub(lookFrom, lookAt).normalized();
        u = vUp.cross(w).normalized();
        v = w.cross(u);
    }

    /**
     * Finds the direction of the primary ray based on the pixel coordinates
     * @param i pixel row index
     * @param j pixel column index
     * @param x x offset from pixel center
     * @param y y offset from pixel center
     * @return primary ray
     */
    public Ray shootRay(int i, int j, double x, double y) {
        Vector3D vu = Vector3D.mult(u, vWidth);
        Vector3D vv = Vector3D.mult(v.negated(), vHeight);
        Vector3D px = Vector3D.div(vu, imgWidth);
        Vector3D py = Vector3D.div(vv, imgHeight);
        Vector3D direction = Vector3D.sub(lookFrom, Vector3D.mult(w, focalLength));
        direction.sub(Vector3D.div(vu, 2));
        direction.sub(Vector3D.div(vv, 2));
        direction.add(Vector3D.add(px, py).mult(0.5D));
        Vector3D pixelXVariation = Vector3D.mult(px, i + x);
        Vector3D pixelYVariation = Vector3D.mult(py, j + y);
        direction.add(pixelXVariation).add(pixelYVariation);
        return new Ray(lookFrom, direction.sub(lookFrom));
    }
}
