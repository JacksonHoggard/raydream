package me.jacksonhoggard.raydream.render;

import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.util.Util;

public class Camera {

    private final Vector3D lookFrom;
    private final Vector3D lookAt;
    private Vector3D u, v, w;
    private final Vector3D vUp;
    private final double focalLength;
    private final double aperture;
    private final double fov;
    private final double vHeight;
    private final double vWidth;
    private final int imgWidth;
    private final int imgHeight;

    public Camera(Vector3D lookFrom, Vector3D lookAt, Vector3D vUp, double fov, double aperture, int imgWidth, int imgHeight) {
        this.lookFrom = lookFrom;
        this.lookAt = lookAt;
        this.vUp = vUp;
        this.fov = fov;
        this.focalLength = Vector3D.sub(lookFrom, lookAt).length();
        this.aperture = 1 / aperture;
        this.imgHeight = imgHeight;
        this.imgWidth = imgWidth;
        vHeight = 2 * Math.tan(Math.toRadians(fov) / 2) * focalLength;
        vWidth = vHeight * (imgWidth / (double) imgHeight);
    }

    /**
     * Finds the direction of the primary ray based on the pixel coordinates
     * @param ray the ray to be sent into the scene
     * @param i pixel row index
     * @param j pixel column index
     * @param x x offset from pixel center
     * @param y y offset from pixel center
     */
    public void shootRay(Ray ray, int i, int j, double x, double y) {
        ray.origin().set(getRandomOrigin());
        w = Vector3D.sub(ray.origin(), lookAt).normalized();
        u = vUp.cross(w).normalized();
        v = w.cross(u);
        Vector3D vu = Vector3D.mult(u, vWidth);
        Vector3D vv = Vector3D.mult(v.negated(), vHeight);
        Vector3D px = Vector3D.div(vu, imgWidth);
        Vector3D py = Vector3D.div(vv, imgHeight);
        Vector3D direction = Vector3D.sub(ray.origin(), Vector3D.mult(w, focalLength));
        direction.sub(Vector3D.div(vu, 2));
        direction.sub(Vector3D.div(vv, 2));
        direction.add(Vector3D.add(px, py).mult(0.5D));
        Vector3D pixelXVariation = Vector3D.mult(px, i + x);
        Vector3D pixelYVariation = Vector3D.mult(py, j + y);
        direction.add(pixelXVariation).add(pixelYVariation);
        ray.direction().set(direction.sub(ray.origin()));
    }

    public Vector3D getRandomOrigin() {
        Vector3D randomOrigin = new Vector3D(Util.randomRange(-aperture, aperture), Util.randomRange(-aperture, aperture), 0);
        while(randomOrigin.length() * randomOrigin.length() >= aperture)
            randomOrigin.set(new Vector3D(Util.randomRange(-aperture, aperture), Util.randomRange(-aperture, aperture), 0));
        randomOrigin.add(lookFrom);
        Vector3D direction = Vector3D.sub(lookAt, randomOrigin).normalize();
        return Vector3D.sub(lookAt, direction.mult(focalLength));
    }

    public double getAperture() {
        return 1 / aperture;
    }

    public double getFocalLength() {
        return focalLength;
    }

    public Vector3D getLookFrom() {
        return lookFrom;
    }

    public Vector3D getLookAt() {
        return lookAt;
    }

    public Vector3D getVUp() {
        return vUp;
    }

    public double getFov() {
        return fov;
    }

    public int getImgWidth() {
        return imgWidth;
    }

    public int getImgHeight() {
        return imgHeight;
    }
}
