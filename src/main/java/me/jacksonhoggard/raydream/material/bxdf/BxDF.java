package me.jacksonhoggard.raydream.material.bxdf;

import java.util.HashMap;

import me.jacksonhoggard.raydream.math.Frame;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.util.MathUtils;

public abstract class BxDF implements IBxDF {

    public enum Event {
        REFLECT, TRANSMIT
    }

    public record BxDFSample(
            Vector3D wi, // Sampled direction (wi)
            Vector3D f, // BSDF value for the sampled direction
            double pdf, // pdf in solid angle
            Event event, // Type of event (reflection or transmission)
            boolean isDelta // If the sampled lobe was (nearly) delta
    ) {
    }

    public record Refraction(
            Vector3D wt, // Refracted direction
            double eta, // Relative IOR (eta_i / eta_t)
            boolean tir // If total internal reflection occurred
    ) {
    }

    protected final HashMap<String, Object> parameters;

    protected final Vector3D baseColor; // 0..1
    protected final Vector3D ng; // Geometric normal
    protected final Vector3D ns; // Shading normal
    protected final Frame shadingFrame; // Frame for shading normal (ns aligned with z axis)

    public BxDF(
            Vector3D ng, Vector3D ns,
            Vector3D baseColor,
            HashMap<String, Object> parameters
    ) {
        this.ng = ng.normalized();
        this.ns = ns.normalized();
        this.baseColor = new Vector3D(
                Math.clamp(baseColor.x, 0.0D, 1.0D),
                Math.clamp(baseColor.y, 0.0D, 1.0D),
                Math.clamp(baseColor.z, 0.0D, 1.0D));
        this.parameters = parameters;
        this.shadingFrame = Frame.fromZ(this.ns);
    }

    public HashMap<String, Object> getParameters() {
        return parameters;
    }

    public Frame getShadingFrame() {
        return shadingFrame;
    }

    // --------------------------------------------------------------------
    // Sampling helpers
    // --------------------------------------------------------------------
    protected static Vector3D sampleVndfGGX(Vector3D wo, double alpha) {
        Vector3D woStd = new Vector3D(alpha * wo.x, alpha * wo.y, wo.z).normalized();
        Vector3D wmStd = sampleVndfHemisphere(woStd);
        Vector3D wm = new Vector3D(wmStd.x * alpha, wmStd.y * alpha, wmStd.z).normalized();
        return wm;
    }

    protected static Vector3D sampleVndfHemisphere(Vector3D wo) {
        double phi = 2.0D * Math.PI * MathUtils.random();
        double z = Math.fma(1.0D - MathUtils.random(), 1.0D + wo.z, -wo.z);
        double sinTheta = Math.sqrt(Math.clamp(1.0D - z * z, 0.0D, 1.0D));
        double x = sinTheta * Math.cos(phi);
        double y = sinTheta * Math.sin(phi);
        Vector3D c = new Vector3D(x, y, z);
        Vector3D h = Vector3D.add(c, wo);
        return h;
    }

    protected static Vector3D sampleGTR1(double a) {
        // Invert CDF for GTR1 over theta (Disney 2012) — approximate
        double u1 = MathUtils.random();
        double u2 = MathUtils.random();
        double a2 = a * a;
        double cosHElevation = Math.sqrt((1.0 - Math.pow(a2, 1.0 - u1)) / (1.0 - a2));
        double hElevation = Math.acos(cosHElevation);
        double hAzimuth = 2.0D * Math.PI * u2;
        double sinHElevation = Math.sin(hElevation);
        double cosHAzimuth = Math.cos(hAzimuth);
        double sinHAzimuth = Math.sin(hAzimuth);
        Vector3D h = new Vector3D(sinHElevation * cosHAzimuth, sinHElevation * sinHAzimuth, cosHElevation);
        return h;
    }

    protected static Vector3D sampleCosineHemisphere() {
        double u1 = MathUtils.random();
        double u2 = MathUtils.random();
        double r = Math.sqrt(u1);
        double theta = 2.0D * Math.PI * u2;
        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        return new Vector3D(x, y, Math.sqrt(Math.max(0.0D, 1.0D - u1)));
    }

    protected static double cosineHemispherePdf(double cos) {
        return cos / Math.PI;
    }

    // --------------------------------------------------------------------
    // Fresnel
    // --------------------------------------------------------------------
    protected static double fresnelDielectricExact(double cosI, double eta) {
        cosI = MathUtils.clamp(cosI, -1.0, 1.0);
        if(cosI < 0) {
            eta = 1.0 / eta;
            cosI = -cosI;
        }
        double sin2I = 1.0 - sqr(cosI);
        double sin2T = sin2I / sqr(eta);
        if(sin2T >= 1.0)
            return 1.0; // TIR
        double cosT = Math.sqrt(Math.max(0.0D, 1.0 - sin2T));
        double rParl = (eta * cosI - cosT) / (eta * cosI + cosT);
        double rPerp = (cosI - eta * cosT) / (cosI + eta * cosT);
        return (sqr(rParl) + sqr(rPerp)) / 2.0D;
    }

    // --------------------------------------------------------------------
    // Small utilities
    // --------------------------------------------------------------------
    protected static Vector3D computeTint(Vector3D c) {
        double lum = MathUtils.luminance(c);
        return (lum > 0.0) ? Vector3D.div(c, lum) : new Vector3D(1);
        // (normalize hue for tinting)
    }

    protected static Vector3D clamp3(Vector3D c) {
        return new Vector3D(Math.max(0.0, Math.min(1.0, c.x)),
                Math.max(0.0, Math.min(1.0, c.y)),
                Math.max(0.0, Math.min(1.0, c.z)));
    }

    protected static double sqr(double x) {
        return x * x;
    }

    protected static double pow5(double x) {
        double x2 = x * x;
        return x2 * x2 * x;
    }

    protected static double mix(double a, double b, double t) {
        return a * (1.0 - t) + b * t;
    }

    protected static Vector3D lerp3(Vector3D a, Vector3D b, double t) {
        return Vector3D.add(Vector3D.mult(a, 1 - t), Vector3D.mult(b, t));
    }

    protected static Vector3D reflect(Vector3D wo, Vector3D n) {
        return wo.negated().add(Vector3D.mult(2.0D * wo.dot(n), n));
    }

    protected static Vector3D reflect(Vector3D wo) {
        return new Vector3D(-wo.x, -wo.y, wo.z);
    }

    protected static Refraction refract(Vector3D wi, Vector3D n, double eta) {
        double cosI = n.dot(wi);
        Vector3D normal = new Vector3D(n);
        if(cosI < 0) {
            eta = 1.0 / eta;
            cosI = -cosI;
            normal.negate();
        }
        double sin2I = Math.max(0.0D, 1.0D - cosI * cosI);
        double sin2T = sin2I / sqr(eta);
        if(sin2T >= 1.0D)
            return new Refraction(null, eta, true); // TIR
        double cosT = Math.sqrt(Math.max(0.0D, 1.0D - sin2T));

        Vector3D wt = wi.negated().div(eta).add(Vector3D.mult(cosI / eta - cosT, normal));
        return new Refraction(wt, eta, false);
    }

    protected static double cosTheta(Vector3D w) {
        return w.z;
    }

    protected static double absCosTheta(Vector3D w) {
        return Math.abs(w.z);
    }

    protected static double cos2Theta(Vector3D w) {
        return sqr(w.z);
    }

    protected static double sin2Theta(Vector3D w) {
        return Math.max(0, 1.0 - cos2Theta(w));
    }

    protected static double sinTheta(Vector3D w) {
        return Math.sqrt(sin2Theta(w));
    }

    protected static double tanTheta(Vector3D w) {
        return sinTheta(w) / cosTheta(w);
    }

    protected static double tan2Theta(Vector3D w) {
        return sin2Theta(w) / cos2Theta(w);
    }

    protected static double cosPhi(Vector3D w) {
        double sinTheta = sinTheta(w);
        return (sinTheta == 0) ? 1.0 : MathUtils.clamp(w.x / sinTheta, -1.0, 1.0);
    }

    protected static double sinPhi(Vector3D w) {
        double sinTheta = sinTheta(w);
        return (sinTheta == 0) ? 0.0 : MathUtils.clamp(w.y / sinTheta, -1.0, 1.0);
    }

    protected static double cosDPhi(Vector3D wa, Vector3D wb) {
        double waxy = sqr(wa.x) + sqr(wa.y);
        double wbxy = sqr(wb.x) + sqr(wb.y);
        if (waxy == 0 || wbxy == 0) return 1.0D;
        return MathUtils.clamp((wa.x * wb.x + wa.y * wb.y) / Math.sqrt(waxy * wbxy), -1.0, 1.0);
    }
}
