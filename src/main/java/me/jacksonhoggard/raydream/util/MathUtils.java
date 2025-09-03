package me.jacksonhoggard.raydream.util;

import me.jacksonhoggard.raydream.config.ApplicationConfig;
import me.jacksonhoggard.raydream.math.Matrix4D;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.math.Vector4D;

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
     * Generates a random unit vector on the unit sphere.
     * @return random unit vector
     */
    public static Vector3D randomUnitVector() {
        Vector3D v = new Vector3D();
        double d = 0.0;
        do {
            v.x = random(-1.0, 1.0);
            v.y = random(-1.0, 1.0);
            v.z = random(-1.0, 1.0);
            d = v.length();
        } while (d > 1.0D);
        return v.div(d);
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

    public static Vector3D transformNormalToWS(Vector3D normal, Matrix4D normalMatrix) {
        Vector4D normalWS = new Vector4D(normal.x, normal.y, normal.z, 0);
        normalWS = normalWS.mult(normalMatrix);
        return new Vector3D(normalWS.x, normalWS.y, normalWS.z).normalize();
    }

    public static Vector3D transformPointToOS(Vector3D point, Matrix4D inverseTransformMatrix) {
        Vector4D pointWS = new Vector4D(point.x, point.y, point.z, 1);
        Vector4D pointOS = pointWS.mult(inverseTransformMatrix);
        return new Vector3D(pointOS.x, pointOS.y, pointOS.z);
    }

    public static Vector3D transformPointToWS(Vector3D point, Matrix4D transformMatrix) {
        Vector4D pointWS = new Vector4D(point.x, point.y, point.z, 1);
        pointWS = pointWS.mult(transformMatrix);
        return new Vector3D(pointWS.x, pointWS.y, pointWS.z);
    }

    public static Vector3D transformDirectionToOS(Vector3D direction, Matrix4D inverseTransformMatrix) {
        Vector4D directionOS = new Vector4D(direction.x, direction.y, direction.z, 0);
        directionOS = directionOS.mult(inverseTransformMatrix);
        return new Vector3D(directionOS.x, directionOS.y, directionOS.z).normalize();
    }

    public static Vector3D transformDirectionToWS(Vector3D direction, Matrix4D transformMatrix) {
        Vector4D directionWS = new Vector4D(direction.x, direction.y, direction.z, 0);
        directionWS = directionWS.mult(transformMatrix);
        return new Vector3D(directionWS.x, directionWS.y, directionWS.z).normalize();
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
     * Refracts a vector through a microfacet with given parameters.
     * @param wi the incident vector (normalized)
     * @param m the microfacet normal (normalized)
     * @param eta the refractive index ratio (eta_incident / eta_transmitted)
     * @param wt the transmitted vector (output)
     * @return true if refraction occurred, false if total internal reflection happened
     */
    public static boolean refractThroughMicrofacet(Vector3D wi, Vector3D m, double eta, Vector3D wt) {
        double cosI = wi.dot(m);
        double sin2I = Math.max(0.0D, 1.0D - cosI*cosI);
        double sin2T = eta*eta * sin2I;
        if(sin2T >= 1.0D) // total internal reflection
            return false;
        
        double cosT = Math.sqrt(Math.max(0.0D, 1.0D - sin2T));
        wt.set(
            Vector3D.add(
                Vector3D.mult(wi, -eta),
                Vector3D.mult(m, eta * cosI - cosT)
            ).normalize()
        );

        return true;
    }

    /**
     * Calculates Fresnel reflectance using Schlick's approximation.
     * @param cosine cosine of incident angle
     * @param refractiveIndex refractive index
     * @return Fresnel reflectance [0.0, 1.0]
     */
    public static double schlickFresnel(double refractiveIndex, double cosine) {
        double r0 = (1.0 - refractiveIndex) / (1.0 + refractiveIndex);
        r0 = r0 * r0;
        return r0 + (1.0 - r0) * Math.pow(1.0 - cosine, 5.0);
    }

    /**
     * Calculates the full Fresnel reflectance for a dielectric material using the complete Fresnel equations.
     * @param cosThetaI cosine of the incident angle
     * @param ni refractive index of the incident medium
     * @param nt refractive index of the transmitted medium
     * @return Fresnel reflectance [0.0, 1.0]
     */
    public static double dielectric(double cosThetaI, double ni, double nt) {
        cosThetaI = clamp(cosThetaI, -1.0, 1.0);

        // Swap index of refraction if this is coming from inside the surface
        if (cosThetaI < 0.0) {
            double temp = ni;
            ni = nt;
            nt = temp;

            cosThetaI = -cosThetaI;
        }

        double sinThetaI = Math.sqrt(Math.max(0.0, 1.0 - cosThetaI * cosThetaI));
        double sinThetaT = ni / nt * sinThetaI;

        // Check for total internal reflection
        if (sinThetaT >= 1.0) {
            return 1.0;
        }

        double cosThetaT = Math.sqrt(Math.max(0.0, 1.0 - sinThetaT * sinThetaT));

        double rParallel = ((nt * cosThetaI) - (ni * cosThetaT)) / ((nt * cosThetaI) + (ni * cosThetaT));
        double rPerpendicular = ((ni * cosThetaI) - (nt * cosThetaT)) / ((ni * cosThetaI) + (nt * cosThetaT));
        return (rParallel * rParallel + rPerpendicular * rPerpendicular) / 2.0;
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

    // Assume vectors are in a y-up tangent space
    /**
     * Returns the cosine of theta (angle from y-axis) for a vector in tangent space.
     * @param w vector in tangent space
     * @return cosine of theta
     */
    public static double cosTheta(Vector3D w) {
        return w.y;
    }

    /**
     * Returns the squared cosine of theta for a vector in tangent space.
     * @param w vector in tangent space
     * @return squared cosine of theta
     */
    public static double cos2Theta(Vector3D w) {
        return w.y * w.y;
    }

    /**
     * Returns the absolute cosine of theta for a vector in tangent space.
     * @param w vector in tangent space
     * @return absolute cosine of theta
     */
    public static double absCosTheta(Vector3D w) {
        return Math.abs(cosTheta(w));
    }

    /**
     * Returns the squared sine of theta for a vector in tangent space.
     * @param w vector in tangent space
     * @return squared sine of theta
     */
    public static double sin2Theta(Vector3D w) {
        return Math.max(0.0, 1.0 - cos2Theta(w));
    }

    /**
     * Returns the sine of theta for a vector in tangent space.
     * @param w vector in tangent space
     * @return sine of theta
     */
    public static double sinTheta(Vector3D w) {
        return Math.sqrt(sin2Theta(w));
    }

    /**
     * Returns the tangent of theta for a vector in tangent space.
     * @param w vector in tangent space
     * @return tangent of theta
     */
    public static double tanTheta(Vector3D w) {
        return sinTheta(w) / cosTheta(w);
    }

    /**
     * Returns the squared tangent of theta for a vector in tangent space.
     * @param w vector in tangent space
     * @return squared tangent of theta
     */
    public static double tan2Theta(Vector3D w) {
        return sin2Theta(w) / cos2Theta(w);
    }

    /**
     * Returns the cosine of phi (azimuthal angle) for a vector in tangent space.
     * @param w vector in tangent space
     * @return cosine of phi
     */
    public static double cosPhi(Vector3D w) {
        double sinTheta = sinTheta(w);
        return (sinTheta == 0) ? 1.0 : clamp(w.x / sinTheta, -1.0, 1.0);
    }

    /**
     * Returns the sine of phi (azimuthal angle) for a vector in tangent space.
     * @param w vector in tangent space
     * @return sine of phi
     */
    public static double sinPhi(Vector3D w) {
        double sinTheta = sinTheta(w);
        return (sinTheta == 0) ? 1.0 : clamp(w.z / sinTheta, -1.0, 1.0);
    }

    /**
     * Returns the squared cosine of phi for a vector in tangent space.
     * @param w vector in tangent space
     * @return squared cosine of phi
     */
    public static double cos2Phi(Vector3D w) {
        double cosPhi = cosPhi(w);
        return cosPhi * cosPhi;
    }

    /**
     * Returns the squared sine of phi for a vector in tangent space.
     * @param w vector in tangent space
     * @return squared sine of phi
     */
    public static double sin2Phi(Vector3D w) {
        double sinPhi = sinPhi(w);
        return sinPhi * sinPhi;
    }
}
