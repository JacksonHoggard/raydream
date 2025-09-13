package me.jacksonhoggard.raydream.material.bxdf;

import java.util.HashMap;

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

    protected final HashMap<String, Object> parameters;

    protected final Vector3D baseColor; // 0..1
    protected final Vector3D ng; // Geometric normal
    protected final Vector3D ns; // Shading normal

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
    }

    public HashMap<String, Object> getParameters() {
        return parameters;
    }

    // --------------------------------------------------------------------
    // Microfacet distributions and geometry terms
    // --------------------------------------------------------------------
    protected static double D_GTR2(double cosNh, double a) {
        double a2 = a * a;
        double d = (cosNh * cosNh) * (a2 - 1.0) + 1.0;
        return a2 / (Math.PI * d * d + 1e-20);
    }

    // GTR1 for clearcoat
    protected static double D_GTR1(double cosNh, double a) {
        double a2 = a * a;
        double denom = 1.0 + (a2 - 1.0) * (cosNh * cosNh);
        double c = (a2 - 1.0) / (Math.PI * Math.log(a2 + 1e-20));
        return c / (denom + 1e-20);
    }

    protected double G_SmithGGX(Vector3D wo, Vector3D wi, Vector3D N, double a) {
        return G1_GGX(Math.abs(N.dot(wo)), a) * G1_GGX(Math.abs(N.dot(wi)), a);
    }

    protected static double G1_GGX(double cosTheta, double a) {
        if (cosTheta <= 0.0)
            return 0.0;
        double a2 = a * a;
        double tan2 = (1.0 - cosTheta * cosTheta) / (cosTheta * cosTheta + 1e-20);
        double root = Math.sqrt(1.0 + a2 * tan2);
        return 2.0 * cosTheta / (cosTheta + root);
    }

    // --------------------------------------------------------------------
    // Sampling helpers
    // --------------------------------------------------------------------
    protected static Vector3D sampleGGX(Vector3D n, double a) {
        double u1 = MathUtils.random();
        double u2 = MathUtils.random();
        double a2 = a * a;
        double tan2 = a2 * u1 / (1.0 - u1 + 1e-20);
        double cos = 1.0 / Math.sqrt(1.0 + tan2);
        double sin = Math.sqrt(Math.max(0.0, 1.0 - cos * cos));
        double phi = 2.0 * Math.PI * u2;
        Vector3D hLocal = new Vector3D(Math.cos(phi) * sin, Math.sin(phi) * sin, cos);
        return toWorld(n, hLocal);
    }

    protected static Vector3D sampleGTR1(Vector3D n, double a) {
        // Invert CDF for GTR1 over theta (Disney 2012) — approximate
        double u1 = MathUtils.random();
        double u2 = MathUtils.random();
        double a2 = a * a;
        double cos = Math.sqrt((1.0 - Math.pow(a2, 1.0 - u1)) / (1.0 - a2));
        double sin = Math.sqrt(Math.max(0.0, 1.0 - cos * cos));
        double phi = 2.0 * Math.PI * u2;
        Vector3D hLocal = new Vector3D(Math.cos(phi) * sin, Math.sin(phi) * sin, cos);
        return toWorld(n, hLocal);
    }

    protected static Vector3D sampleCosineHemisphere(Vector3D n) {
        double u1 = MathUtils.random();
        double u2 = MathUtils.random();
        double r = Math.sqrt(u1);
        double theta = 2.0 * Math.PI * u2;
        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        double z = Math.sqrt(Math.max(0.0, 1.0 - u1));
        return toWorld(n, new Vector3D(x, y, z));
    }

    protected static double cosineHemispherePdf(double cos) {
        return cos / Math.PI;
    }

    protected static Vector3D toWorld(Vector3D N, Vector3D vLocal) {
        // Build an ONB from N
        Vector3D T = (Math.abs(N.z) < 0.999) ? new Vector3D(0, 0, 1).cross(N).normalized()
                : new Vector3D(0, 1, 0).cross(N).normalized();
        Vector3D B = N.cross(T);
        // vWorld = x*T + y*B + z*N
        return Vector3D
                .add(Vector3D.add(Vector3D.mult(T, vLocal.x), Vector3D.mult(B, vLocal.y)), Vector3D.mult(N, vLocal.z))
                .normalized();
    }

    protected static Vector3D microfacetHalfForRefraction(Vector3D wo, Vector3D wi, double eta) {
        // Heitz convention: h ∝ eta*wi + wo
        Vector3D h = Vector3D.mult(wi, eta).add(wo).normalize();
        if (h == null)
            return null;
        // Ensure h points to same hemisphere as wo (for stability)
        return h;
    }

    // --------------------------------------------------------------------
    // Fresnel
    // --------------------------------------------------------------------
    protected static Vector3D schlickF(Vector3D F0, double cosIt) {
        double x = pow5(1.0 - cosIt);
        return Vector3D.add(F0, Vector3D.mult(new Vector3D(1).sub(F0), x));
    }

    protected static double schlickScalar(double F0, double cosIt) {
        return F0 + (1.0 - F0) * pow5(1.0 - cosIt);
    }

    protected static double fresnelDielectricExact(Vector3D v, Vector3D n, double ior) {
        Vector3D vNeg = v.negated();
        double cosI = Math.clamp(vNeg.dot(n), -1.0, 1.0);
        double etaI = 1.0;
        double etaT = ior;
        if (cosI > 0.0) {
            double temp = etaI;
            etaI = etaT;
            etaT = temp;
        }
        double sinT = etaI / etaT * Math.sqrt(Math.max(0.0D, 1 - cosI * cosI));
        if (sinT >= 1.0)
            return 1.0; // TIR
        double cosT = Math.sqrt(Math.max(0.0D, 1 - sinT * sinT));
        cosI = Math.abs(cosI);
        double Rs = ((etaT * cosI) - (etaI * cosT)) / ((etaT * cosI) + (etaI * cosT));
        double Rp = ((etaI * cosI) - (etaT * cosT)) / ((etaI * cosI) + (etaT * cosT));
        return (Rs * Rs + Rp * Rp) / 2.0;
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

    protected static Vector3D safeNormalize(Vector3D v) {
        double len = v.length();
        if (len <= 0.0)
            return null;
        return Vector3D.div(v, len);
    }

    protected static Vector3D faceforward(Vector3D n, Vector3D v) {
        return (n.dot(v) >= 0.0) ? n : n.negated();
    }

}
