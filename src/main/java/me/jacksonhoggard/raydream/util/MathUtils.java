package me.jacksonhoggard.raydream.util;

import me.jacksonhoggard.raydream.config.ApplicationConfig;
import me.jacksonhoggard.raydream.math.Vector3D;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class with common mathematical and helper functions.
 * Consolidates scattered utility methods and improves performance.
 */
public final class MathUtils {

    public static final double EPSILON = ApplicationConfig.DEFAULT_EPSILON;
    public static final double PI = Math.PI;
    public static final double TWO_PI = 2.0 * PI;
    public static final double HALF_PI = PI * 0.5;
    public static final double DEG_TO_RAD = PI / 180.0;
    public static final double RAD_TO_DEG = 180.0 / PI;

    private MathUtils() {
        // Prevent instantiation
    }

    /**
     * Fast approximate equality check for doubles.
     * @param a first value
     * @param b second value
     * @return true if values are approximately equal
     */
    public static boolean equals(double a, double b) {
        return Math.abs(a - b) < EPSILON;
    }

    /**
     * Fast approximate equality check for doubles with custom epsilon.
     * @param a first value
     * @param b second value
     * @param epsilon tolerance
     * @return true if values are approximately equal
     */
    public static boolean equals(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    /**
     * Clamps a value between min and max.
     * @param value the value to clamp
     * @param min minimum value
     * @param max maximum value
     * @return clamped value
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Linear interpolation between two values.
     * @param a start value
     * @param b end value
     * @param t interpolation factor (0.0 to 1.0)
     * @return interpolated value
     */
    public static double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    /**
     * Linear interpolation between two vectors.
     * @param a start vector
     * @param b end vector
     * @param t interpolation factor (0.0 to 1.0)
     * @return new interpolated vector
     */
    public static Vector3D lerp(Vector3D a, Vector3D b, double t) {
        return new Vector3D(
            lerp(a.x, b.x, t),
            lerp(a.y, b.y, t),
            lerp(a.z, b.z, t)
        );
    }

    /**
     * Generates a random double between 0.0 and 1.0.
     * Uses ThreadLocalRandom for better performance in multithreaded environments.
     * @return random double [0.0, 1.0)
     */
    public static double random() {
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * Generates a random double between min and max.
     * @param min minimum value (inclusive)
     * @param max maximum value (exclusive)
     * @return random double in range [min, max)
     */
    public static double random(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * Generates a random vector with components in [-1, 1].
     * @return random vector
     */
    public static Vector3D randomVector() {
        return new Vector3D(
            random(-1.0, 1.0),
            random(-1.0, 1.0),
            random(-1.0, 1.0)
        );
    }

    /**
     * Generates a random unit vector on the unit sphere.
     * Uses rejection sampling for uniform distribution.
     * @return random unit vector
     */
    public static Vector3D randomUnitVector() {
        Vector3D v;
        do {
            v = randomVector();
        } while (v.dot(v) > 1.0D); // Use dot product with itself instead of lengthSquared
        return v.normalized();
    }

    /**
     * Generates a random vector in the unit hemisphere oriented by the normal.
     * @param normal the hemisphere orientation
     * @return random vector in hemisphere
     */
    public static Vector3D randomHemisphere(Vector3D normal) {
        Vector3D v = randomUnitVector();
        if (v.dot(normal) < 0.0D) {
            v.negate();
        }
        return v;
    }

    /**
     * Reflects a vector about a normal.
     * @param incident the incident vector
     * @param normal the surface normal
     * @return reflected vector
     */
    public static Vector3D reflect(Vector3D incident, Vector3D normal) {
        return Vector3D.sub(incident, Vector3D.mult(normal, 2.0 * incident.dot(normal)));
    }

    /**
     * Refracts a vector through a surface with given refractive indices.
     * @param incident the incident vector (normalized)
     * @param normal the surface normal (normalized)
     * @param etaRatio ratio of refractive indices (eta_incident / eta_transmitted)
     * @return refracted vector or null for total internal reflection
     */
    public static Vector3D refract(Vector3D incident, Vector3D normal, double etaRatio) {
        double cosI = -incident.dot(normal);
        double sinT2 = etaRatio * etaRatio * (1.0 - cosI * cosI);

        if (sinT2 > 1.0) {
            return null; // Total internal reflection
        }

        double cosT = Math.sqrt(1.0 - sinT2);
        return Vector3D.add(Vector3D.mult(incident, etaRatio), Vector3D.mult(normal, etaRatio * cosI - cosT));
    }

    /**
     * Calculates Fresnel reflectance using Schlick's approximation.
     * @param cosine cosine of incident angle
     * @param refractiveIndex refractive index
     * @return Fresnel reflectance [0.0, 1.0]
     */
    public static double schlickFresnel(double cosine, double refractiveIndex) {
        double r0 = (1.0 - refractiveIndex) / (1.0 + refractiveIndex);
        r0 = r0 * r0;
        return r0 + (1.0 - r0) * Math.pow(1.0 - cosine, 5.0);
    }

    /**
     * Converts degrees to radians.
     * @param degrees angle in degrees
     * @return angle in radians
     */
    public static double toRadians(double degrees) {
        return degrees * DEG_TO_RAD;
    }

    /**
     * Converts radians to degrees.
     * @param radians angle in radians
     * @return angle in degrees
     */
    public static double toDegrees(double radians) {
        return radians * RAD_TO_DEG;
    }
}
