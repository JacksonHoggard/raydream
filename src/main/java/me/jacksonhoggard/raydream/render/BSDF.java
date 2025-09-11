package me.jacksonhoggard.raydream.render;

import me.jacksonhoggard.raydream.util.MathUtils;

import java.util.Random;

import me.jacksonhoggard.raydream.math.Vector3D;

public class BSDF {

    public enum Event {
        REFLECT, TRANSMIT
    }

    public enum Lobe {
        DIFFUSE, SPECULAR, CLEARCOAT, TRANSMIT_SPECULAR
    }

    public record BSDFSample(
            Vector3D l, // Sampled direction (wi)
            Vector3D f, // BSDF value for the sampled direction
            double pdf, // Mixture pdf over reflection/transmission in solid angle
            Event event, // Type of event (reflection or transmission)
            double etaI, // Incident IOR
            double etaT, // Transmitted IOR
            boolean isDelta // If the sampled lobe was (nearly) delta
    ) {
    }

    // --- Material parameters (Disney 2012/2015 set, simplified) ---
    private final Vector3D baseColor; // 0..1
    private final double metallic; // 0..1
    private final double subsurface; // 0..1 (used lightly in diffuse)
    private final double specular; // 0..1 (~index via 0..1 knob)
    private final double roughness; // 0..1
    private final double specularTint; // 0..1
    private final double sheen; // 0..1
    private final double sheenTint; // 0..1
    private final double clearcoat; // 0..1
    private final double clearcoatGloss; // 0..1
    private final double transmission; // 0..1 (dielectric glass)
    private final double ior; // index of refraction for transmission
    private final boolean thin; // if true: thin sheet approximation for transmission (optional)
    private final Vector3D n; // shading normal (unit)

    // Precomputed values for the BSDF
    private final Vector3D specularColorF0; // Colored F0
    private final double alpha; // GGX alpha
    private final double alphaCoat; // GTR1 alpha for clearcoat
    private final Vector3D sheenColor;

    public BSDF(
            Vector3D n,
            Vector3D baseColor,
            double metallic, double subsurface, double specular, double roughness,
            double specularTint, double sheen, double sheenTint,
            double clearcoat, double clearcoatGloss,
            double transmission, double ior,
            boolean thin) {
        this.n = n.normalized();
        this.baseColor = new Vector3D(
                Math.clamp(baseColor.x, 0.0D, 1.0D),
                Math.clamp(baseColor.y, 0.0D, 1.0D),
                Math.clamp(baseColor.z, 0.0D, 1.0D));
        this.metallic = Math.clamp(metallic, 0.0D, 1.0D);
        this.subsurface = Math.clamp(subsurface, 0.0D, 1.0D);
        this.specular = Math.clamp(specular, 0.0D, 1.0D);
        this.roughness = Math.clamp(roughness, 0.0D, 1.0D);
        this.specularTint = Math.clamp(specularTint, 0.0D, 1.0D);
        this.sheen = Math.clamp(sheen, 0.0D, 1.0D);
        this.sheenTint = Math.clamp(sheenTint, 0.0D, 1.0D);
        this.clearcoat = Math.clamp(clearcoat, 0.0D, 1.0D);
        this.clearcoatGloss = Math.clamp(clearcoatGloss, 0.0D, 1.0D);
        this.transmission = Math.clamp(transmission, 0.0D, 1.0D);
        this.ior = Math.max(1.0001, ior);
        this.thin = thin;

        this.alpha = Math.max(0.001, roughness * roughness); // GGX
        // Disney clearcoat uses GTR1 with alpha ~ mix(0.1, 0.001, clearcoatGloss)
        this.alphaCoat = mix(0.1, 0.001, this.clearcoatGloss);

        // Specular F0 (colored): 0.08*specular*(1-specularTint)+tinted term; then blend
        // to baseColor for metallic
        Vector3D tint = computeTint(this.baseColor);
        Vector3D dielectricF0 = Vector3D.add(
                Vector3D.mult(Vector3D.ONE, 0.08 * this.specular * (1.0 - this.specularTint)),
                Vector3D.mult(tint, 0.08 * this.specular * this.specularTint));
        this.specularColorF0 = lerp3(dielectricF0, this.baseColor, this.metallic);

        // Sheen color (tinting toward baseColor hue)
        this.sheenColor = lerp3(Vector3D.ONE, tint, this.sheenTint);
    }

    /**
     * BSDF value f(wo, wi), wo/wi are world-space unit vectors pointing away from
     * the surface.
     */
    public Vector3D eval(Vector3D wo, Vector3D wi) {
        final double EPS = 1e-9D;
        Vector3D N = faceforward(n, wo);
        double cosWo = N.dot(wo);
        double cosWi = N.dot(wi);
        if (Math.abs(cosWo) < EPS || Math.abs(cosWi) < EPS)
            return new Vector3D(0, 0, 0);

        boolean reflect = (cosWo * cosWi) > 0.0;

        Vector3D f = new Vector3D();

        if (reflect) {
            // --- Diffuse (Burley) ---
            if (!isMetal() && transmission < 1.0) {
                f = add3(f, diffuseBurley(wo, wi, N));
                // Sheen (retro-reflection tint)
                if (sheen > 0.0) {
                    double ldotH = MathUtils.saturate(Vector3D.add(wi, wo).normalized().dot(wi));
                    double fsheen = sheen * pow5(1.0 - ldotH);
                    f = add3(f, scale3(sheenColor, fsheen / Math.PI));
                }
            }

            // --- Microfacet specular (GGX) ---
            Vector3D h = safeNormalize(Vector3D.add(wo, wi));
            if (h != null) {
                double cosWoH = Math.abs(wo.dot(h));
                double cosNh = Math.abs(N.dot(h));
                if (cosWoH > 0.0 && cosNh > 0.0) {
                    double D = D_GTR2(cosNh, alpha);
                    double G = G_SmithGGX(wo, wi, N, alpha);
                    Vector3D F = schlickF(specularColorF0, cosWoH);
                    Vector3D fr = scale3(mul3(F, D * G), 1.0 / (4.0 * Math.abs(cosWo) * Math.abs(cosWi)));
                    f = add3(f, fr);
                }
            }

            // --- Clearcoat (GTR1) ---
            if (clearcoat > 0.0) {
                Vector3D hC = safeNormalize(Vector3D.add(wo, wi));
                if (hC != null) {
                    double cosWoH = Math.abs(wo.dot(hC));
                    double cosNh = Math.abs(N.dot(hC));
                    if (cosWoH > 0.0 && cosNh > 0.0) {
                        double Dc = D_GTR1(cosNh, alphaCoat);
                        double Gc = G_SmithGGX(wo, wi, N, 0.25); // Disney uses ~0.25 for clearcoat
                        double Fc = schlickScalar(0.04, cosWoH); // fixed F0 ~ 0.04
                        double kc = 0.25 * clearcoat; // energy scale per Disney
                        double fr = kc * Fc * Dc * Gc / (4.0 * Math.abs(cosWo) * Math.abs(cosWi));
                        f = add3(f, new Vector3D(fr, fr, fr));
                    }
                }
            }
        } else {
            // --- Microfacet transmission (GGX) ---
            if (!isMetal() && transmission > 0.0) {
                double etaI = (cosWo > 0.0) ? 1.0 : ior;
                double etaT = (cosWo > 0.0) ? ior : 1.0;
                double eta = etaI / etaT;

                Vector3D h = microfacetHalfForRefraction(wo, wi, eta);
                if (h != null) {
                    double cosNh = Math.abs(N.dot(h));
                    double D = D_GTR2(cosNh, alpha);
                    double G = G_SmithGGX(wo, wi, N, alpha);

                    double woDotH = wo.dot(h);
                    double wiDotH = wi.dot(h);
                    if (woDotH == 0.0 || wiDotH == 0.0)
                        return new Vector3D();

                    double F = fresnelDielectricExact(Math.abs(woDotH), etaI, etaT); // scalar F for dielectrics
                    // Heitz/Walter microfacet BTDF:
                    double denom = (eta * wiDotH + woDotH);
                    double ftScale = (1.0 - F) * D * G * eta * eta * Math.abs(wiDotH * woDotH)
                            / (Math.abs(cosWi) * Math.abs(cosWo) * denom * denom);

                    // Transmission color: baseColor acts as medium tint; thin sheet keeps color on
                    // refraction
                    Vector3D Tcol = (thin ? baseColor : new Vector3D(1));
                    f = add3(f, scale3(Tcol, transmission * ftScale));
                }
            }
        }
        return f;
    }

    /** Mixture PDF in solid angle (sr^-1) matching how sample() chooses lobes. */
    public double pdf(Vector3D wo, Vector3D wi) {
        final double EPS = 1e-9D;
        Vector3D N = faceforward(n, wo);
        double cosWo = N.dot(wo);
        double cosWi = N.dot(wi);
        if (Math.abs(cosWo) < EPS || Math.abs(cosWi) < EPS)
            return 0.0;

        boolean reflect = (cosWo * cosWi) > 0.0;

        // lobe weights used by sampler
        Weights w = lobeWeights();

        double pdf = 0.0;
        if (reflect) {
            // diffuse
            if (!isMetal() && transmission < 1.0) {
                double p = w.pDiffuse;
                if (p > 0.0)
                    pdf += p * cosineHemispherePdf(Math.abs(cosWi));
            }
            // specular (GGX)
            if (w.pSpecular > 0.0) {
                Vector3D h = safeNormalize(Vector3D.add(wo, wi));
                if (h != null) {
                    double cosNh = Math.abs(N.dot(h));
                    double woDotH = Math.abs(wo.dot(h));
                    if (woDotH > 0.0 && cosNh > 0.0) {
                        double p = w.pSpecular;
                        double pdfH = D_GTR2(cosNh, alpha) * cosNh; // pdf over h
                        double pdfW = pdfH / (4.0 * woDotH); // change of variables
                        pdf += p * pdfW;
                    }
                }
            }
            // clearcoat (GTR1)
            if (w.pClearcoat > 0.0) {
                Vector3D hC = safeNormalize(Vector3D.add(wo, wi));
                if (hC != null) {
                    double cosNh = Math.abs(N.dot(hC));
                    double woDotH = Math.abs(wo.dot(hC));
                    if (woDotH > 0.0 && cosNh > 0.0) {
                        double p = w.pClearcoat;
                        double pdfH = D_GTR1(cosNh, alphaCoat) * cosNh;
                        double pdfW = pdfH / (4.0 * woDotH);
                        pdf += p * pdfW;
                    }
                }
            }
        } else {
            // transmission (GGX)
            if (!isMetal() && transmission > 0.0 && w.pTransmission > 0.0) {
                double etaI = (cosWo > 0.0) ? 1.0 : ior;
                double etaT = (cosWo > 0.0) ? ior : 1.0;
                double eta = etaI / etaT;

                Vector3D h = microfacetHalfForRefraction(wo, wi, eta);
                if (h != null) {
                    double cosNh = Math.abs(N.dot(h));
                    double wiDotH = Math.abs(wi.dot(h));
                    double woDotH = Math.abs(wo.dot(h));
                    double denom = (eta * wiDotH + woDotH);
                    if (cosNh > 0.0 && denom != 0.0) {
                        // pdf(h) * Jacobian from h->wi for refraction:
                        double pdfH = D_GTR2(cosNh, alpha) * cosNh;
                        double pdfW = pdfH * (wiDotH / (denom * denom));
                        pdf += w.pTransmission * pdfW;
                    }
                }
            }
        }
        return pdf;
    }

    /** Sample the lobe mixture; returns null if something degenerate occurs. */
    public BSDFSample sample(Vector3D wo) {
        final double EPS = 1e-9D;
        Vector3D N = faceforward(n, wo);
        double cosWo = N.dot(wo);
        if (Math.abs(cosWo) < EPS)
            return null;

        Weights w = lobeWeights();
        double xi = MathUtils.random();

        // Partition [0,1) by weights. Only include lobes that can contribute.
        double cum = 0.0;

        // Prefer reflection when we pick a reflection lobe; transmission handled below
        // with Fresnel split.
        // 1) Diffuse
        if (!isMetal() && transmission < 1.0 && w.pDiffuse > 0.0) {
            double next = cum + w.pDiffuse;
            if (xi < next) {
                Vector3D wi = sampleCosineHemisphere(N);
                if (wi == null)
                    return null;
                if ((N.dot(wi) > 0.0) != (cosWo > 0.0)) wi = wi.negated(); // align hemisphere
                Vector3D f = eval(wo, wi);
                double p = pdf(wo, wi);
                if (p <= 0.0)
                    return null;
                return new BSDFSample(wi, f, p, Event.REFLECT, 1.0, 1.0, false);
            }
            cum = next;
        }

        // 2) Clearcoat
        if (w.pClearcoat > 0.0) {
            double next = cum + w.pClearcoat;
            if (xi < next) {
                Vector3D h = sampleGTR1(N, alphaCoat);
                if (h == null)
                    return null;
                // Make h share hemisphere with wo
                if (N.dot(h) < 0.0)
                    h = h.negated();

                Vector3D wi = reflect(wo, h);
                if (wi == null)
                    return null;

                Vector3D f = eval(wo, wi);
                double p = pdf(wo, wi);
                if (p <= 0.0)
                    return null;
                boolean delta = alphaCoat < 1e-4;
                return new BSDFSample(wi, f, p, Event.REFLECT, 1.0, 1.0, delta);
            }
            cum = next;
        }

        // 3) Specular or Transmission via GGX
        // We decide reflection vs refraction using exact dielectric Fresnel at the
        // sampled microfacet.
        // If metallic or transmission == 0, this collapses to reflection only.
        {
            Vector3D h = sampleGGX(N, alpha);
            if (h == null)
                return null;
            // Make h share hemisphere with wo
            if (N.dot(h) < 0.0) h = h.negated();

            double etaI = (cosWo > 0.0) ? 1.0 : ior;
            double etaT = (cosWo > 0.0) ? ior : 1.0;
            double eta = etaI / etaT;

            double pr = w.pSpecular; // base prob mass for specular bucket
            double pt = (!isMetal() ? w.pTransmission : 0.0);

            double sumRT = pr + pt;
            if (sumRT <= 0.0)
                return null;

            double xiRT = (MathUtils.random()) * sumRT;
            boolean chooseRefl = (xiRT < pr) || (transmission <= 0.0) || isMetal();

            Vector3D wi;
            Event ev;

            if (chooseRefl) {
                wi = reflect(wo, h);
                if (wi == null)
                    return null;
                ev = Event.REFLECT;
            } else {
                wi = refract(wo.negated(), h, eta);
                if (wi == null) {
                    // TIR: fallback to reflection
                    wi = reflect(wo, h);
                    ev = Event.REFLECT;
                } else {
                    ev = Event.TRANSMIT;
                }
            }

            Vector3D f = eval(wo, wi);
            double p = pdf(wo, wi);
            if (p <= 0.0)
                return null;
            boolean delta = alpha < 1e-4;
            return new BSDFSample(wi, f, p, ev, etaI, etaT, delta);
        }
    }

    // --------------------------------------------------------------------
    // Internal: lobe weights for sampling / pdf mixture (simple, robust).
    // --------------------------------------------------------------------
    private static final class Weights {
        double pDiffuse, pSpecular, pClearcoat, pTransmission;
    }

    private Weights lobeWeights() {
        Weights w = new Weights();
        double nonMetal = 1.0 - metallic;

        // Diffuse bucket only for non-metals and when we aren't fully transmissive
        w.pDiffuse = nonMetal * (1.0 - transmission);

        // Specular is always present (even for metals)
        // Heuristic: weight by average F0 to prefer mirrors at grazing angles
        double f0 = luminance(specularColorF0);
        w.pSpecular = 0.5 + 1.5 * f0; // simple bias toward specular

        // Clearcoat small extra lobe
        w.pClearcoat = 0.25 * clearcoat;

        // Transmission only for non-metals
        w.pTransmission = nonMetal * transmission;

        double sum = w.pDiffuse + w.pSpecular + w.pClearcoat + w.pTransmission;
        if (sum > 0.0) {
            w.pDiffuse /= sum;
            w.pSpecular /= sum;
            w.pClearcoat /= sum;
            w.pTransmission /= sum;
        }
        return w;
    }

    // --------------------------------------------------------------------
    // Diffuse (Burley), Sheen helpers
    // --------------------------------------------------------------------
    private Vector3D diffuseBurley(Vector3D wo, Vector3D wi, Vector3D N) {
        double cosWo = Math.abs(N.dot(wo));
        double cosWi = Math.abs(N.dot(wi));
        Vector3D h = safeNormalize(Vector3D.add(wo, wi));
        double ldh2 = (h != null) ? sqr(MathUtils.saturate(wi.dot(h))) : 0.0;

        double FD90 = 0.5 + 2.0 * ldh2 * roughness;
        double FL = 1.0 + (FD90 - 1.0) * pow5(1.0 - cosWi);
        double FV = 1.0 + (FD90 - 1.0) * pow5(1.0 - cosWo);

        // simple subsurface-ish boost (Disney blends in a modified diffuse; keep
        // minimal)
        double ss = mix(1.0, 1.25, subsurface);

        return scale3(baseColor, ss * FL * FV / Math.PI);
    }

    // --------------------------------------------------------------------
    // Microfacet distributions and geometry terms
    // --------------------------------------------------------------------
    private static double D_GTR2(double cosNh, double a) {
        double a2 = a * a;
        double d = (cosNh * cosNh) * (a2 - 1.0) + 1.0;
        return a2 / (Math.PI * d * d + 1e-20);
    }

    // GTR1 for clearcoat
    private static double D_GTR1(double cosNh, double a) {
        double a2 = a * a;
        double denom = 1.0 + (a2 - 1.0) * (cosNh * cosNh);
        double c = (a2 - 1.0) / (Math.PI * Math.log(a2 + 1e-20));
        return c / (denom + 1e-20);
    }

    private double G_SmithGGX(Vector3D wo, Vector3D wi, Vector3D N, double a) {
        return G1_GGX(Math.abs(N.dot(wo)), a) * G1_GGX(Math.abs(N.dot(wi)), a);
    }

    private static double G1_GGX(double cosTheta, double a) {
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
    private static Vector3D sampleGGX(Vector3D N, double a) {
        double u1 = MathUtils.random();
        double u2 = MathUtils.random();
        double a2 = a * a;
        double tan2 = a2 * u1 / (1.0 - u1 + 1e-20);
        double cos = 1.0 / Math.sqrt(1.0 + tan2);
        double sin = Math.sqrt(Math.max(0.0, 1.0 - cos * cos));
        double phi = 2.0 * Math.PI * u2;
        Vector3D hLocal = new Vector3D(Math.cos(phi) * sin, Math.sin(phi) * sin, cos);
        return toWorld(N, hLocal);
    }

    private static Vector3D sampleGTR1(Vector3D N, double a) {
        // Invert CDF for GTR1 over theta (Disney 2012) — approximate
        double u1 = MathUtils.random();
        double u2 = MathUtils.random();
        double a2 = a * a;
        double cos = Math.sqrt((1.0 - Math.pow(a2, 1.0 - u1)) / (1.0 - a2));
        double sin = Math.sqrt(Math.max(0.0, 1.0 - cos * cos));
        double phi = 2.0 * Math.PI * u2;
        Vector3D hLocal = new Vector3D(Math.cos(phi) * sin, Math.sin(phi) * sin, cos);
        return toWorld(N, hLocal);
    }

    private static Vector3D sampleCosineHemisphere(Vector3D N) {
        double u1 = MathUtils.random();
        double u2 = MathUtils.random();
        double r = Math.sqrt(u1);
        double theta = 2.0 * Math.PI * u2;
        double x = r * Math.cos(theta);
        double y = r * Math.sin(theta);
        double z = Math.sqrt(Math.max(0.0, 1.0 - u1));
        return toWorld(N, new Vector3D(x, y, z));
    }

    private static double cosineHemispherePdf(double cos) {
        return cos / Math.PI;
    }

    private static Vector3D toWorld(Vector3D N, Vector3D vLocal) {
        // Build an ONB from N
        Vector3D T = (Math.abs(N.z) < 0.999) ? new Vector3D(0, 0, 1).cross(N).normalized()
                : new Vector3D(0, 1, 0).cross(N).normalized();
        Vector3D B = N.cross(T);
        // vWorld = x*T + y*B + z*N
        return Vector3D
                .add(Vector3D.add(Vector3D.mult(T, vLocal.x), Vector3D.mult(B, vLocal.y)), Vector3D.mult(N, vLocal.z))
                .normalized();
    }

    private static Vector3D reflect(Vector3D v, Vector3D m) {
        // Reflect "v" across microfacet normal m (v points AWAY from the surface).
        // Correct: r = -v + 2 * dot(v, m) * m
        return Vector3D.add(Vector3D.mult(m, 2.0 * v.dot(m)), v.negated()).normalized();
    }

    private static Vector3D refract(Vector3D v, Vector3D m, double eta) {
        // Refract v across normal m with ratio eta = etaI/etaT
        double cosI = clampN1P1(v.dot(m));
        double sin2I = Math.max(0.0, 1.0 - cosI * cosI);
        double sin2T = eta * eta * sin2I;
        if (sin2T >= 1.0)
            return null; // TIR
        double cosT = Math.sqrt(Math.max(0.0, 1.0 - sin2T));
        // Note: v points "from" surface; Snell: t = -eta*v + (eta*cosI - cosT)*m
        Vector3D t = Vector3D.mult(m, eta * cosI - cosT).sub(Vector3D.mult(v, eta));
        return t.normalized();
    }

    private static Vector3D microfacetHalfForRefraction(Vector3D wo, Vector3D wi, double eta) {
        // Heitz convention: h ∝ eta*wi + wo
        Vector3D h = Vector3D.mult(wi, eta).add(wo).normalize();
        if (h == null)
            return null;
        // Ensure h points to same hemisphere as macro normal (for stability)
        return h;
    }

    // --------------------------------------------------------------------
    // Fresnel
    // --------------------------------------------------------------------
    private static Vector3D schlickF(Vector3D F0, double cosIt) {
        double x = pow5(1.0 - cosIt);
        return add3(F0, scale3(new Vector3D(1).sub(F0), x));
    }

    private static double schlickScalar(double F0, double cosIt) {
        return F0 + (1.0 - F0) * pow5(1.0 - cosIt);
    }

    private static double fresnelDielectricExact(double cosI, double etaI, double etaT) {
        cosI = clamp01(cosI);
        double sin2I = Math.max(0.0, 1.0 - cosI * cosI);
        double eta = etaI / etaT;
        double sin2T = eta * eta * sin2I;
        if (sin2T >= 1.0)
            return 1.0; // TIR
        double cosT = Math.sqrt(Math.max(0.0, 1.0 - sin2T));
        double Rs = ((etaT * cosI) - (etaI * cosT)) / ((etaT * cosI) + (etaI * cosT));
        double Rp = ((etaI * cosI) - (etaT * cosT)) / ((etaI * cosI) + (etaT * cosT));
        return 0.5 * (Rs * Rs + Rp * Rp);
    }

    // --------------------------------------------------------------------
    // Small utilities
    // --------------------------------------------------------------------
    private boolean isMetal() {
        return metallic >= 0.999;
    }

    private static Vector3D computeTint(Vector3D c) {
        double lum = luminance(c);
        return (lum > 0.0) ? Vector3D.div(c, lum) : new Vector3D(1);
        // (normalize hue for tinting)
    }

    private static double luminance(Vector3D c) {
        return 0.2126 * c.x + 0.7152 * c.y + 0.0722 * c.z;
    }

    private static Vector3D clamp3(Vector3D c) {
        return new Vector3D(clamp01(c.x), clamp01(c.y), clamp01(c.z));
    }

    private static double clamp01(double x) {
        return Math.max(0.0, Math.min(1.0, x));
    }

    private static double clampN1P1(double x) {
        return Math.max(-1.0, Math.min(1.0, x));
    }

    private static double sqr(double x) {
        return x * x;
    }

    private static double pow5(double x) {
        double x2 = x * x;
        return x2 * x2 * x;
    }

    private static double mix(double a, double b, double t) {
        return a * (1.0 - t) + b * t;
    }

    private static Vector3D lerp3(Vector3D a, Vector3D b, double t) {
        return add3(scale3(a, 1 - t), scale3(b, t));
    }

    private static Vector3D add3(Vector3D a, Vector3D b) {
        return new Vector3D(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    private static Vector3D mul3(Vector3D a, double s) {
        return new Vector3D(a.x * s, a.y * s, a.z * s);
    }

    private static Vector3D scale3(Vector3D a, double s) {
        return mul3(a, s);
    }

    private static Vector3D mul3(Vector3D a, Vector3D b) {
        return new Vector3D(a.x * b.x, a.y * b.y, a.z * b.z);
    }

    private static Vector3D safeNormalize(Vector3D v) {
        double len = v.length();
        if (len <= 0.0)
            return null;
        return Vector3D.div(v, len);
    }

    private static Vector3D faceforward(Vector3D n, Vector3D v) {
        return (n.dot(v) >= 0.0) ? n : n.negated();
    }

}
