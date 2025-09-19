package me.jacksonhoggard.raydream.material.bxdf.disney;

import java.util.HashMap;

import me.jacksonhoggard.raydream.material.bxdf.BSDF;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.util.MathUtils;

public class DisneyGlass extends BSDF {

    private final double ior;
    private final double roughness; // [0..1]
    private final double anisotropic; // [0..1]

    public DisneyGlass(
            Vector3D ng, Vector3D ns,
            Vector3D baseColor,
            HashMap<String, Object> parameters) {
        super(ng, ns, baseColor, parameters);
        this.ior = ((Double) parameters.get("ior"));
        this.roughness = ((Double) parameters.get("roughness"));
        this.anisotropic = ((Double) parameters.get("anisotropic"));
    }

    @Override
    public Vector3D eval(Vector3D wo, Vector3D wi) {
        if (ng.dot(wi) * ng.dot(wo) > 0) {
            // Reflection
            Vector3D h = Vector3D.add(wo, wi).normalized();
            double Fg = fresnelDielectricExact(wo, h, ior);

            Vector3D hLocal = toLocal(h, ns).normalize();
            Vector3D woLocal = toLocal(wo, ns).normalize();
            Vector3D wiLocal = toLocal(wi, ns).normalize();

            double aspect = Math.sqrt(1.0D - 0.9D * anisotropic);
            double ax = Math.max(0.0001D, sqr(roughness) / aspect);
            double ay = Math.max(0.0001D, sqr(roughness) * aspect);

            double D = 1.0D / (Math.PI * ax * ay * Math.pow(
                    (hLocal.x * hLocal.x) / (ax * ax) +
                            (hLocal.y * hLocal.y) / (ay * ay) +
                            (hLocal.z * hLocal.z),
                    2));

            double lambdaL = (Math.sqrt(1.0 + (sqr(wiLocal.x * ax) + sqr(wiLocal.y * ay)) / sqr(wiLocal.z)) - 1.0D)
                    / 2.0D;
            double lambdaV = (Math.sqrt(1.0 + (sqr(woLocal.x * ax) + sqr(woLocal.y * ay)) / sqr(woLocal.z)) - 1.0D)
                    / 2.0D;
            double GL = 1.0D / (1.0D + lambdaL);
            double GV = 1.0D / (1.0D + lambdaV);
            double G = GL * GV;
            return Vector3D.mult(baseColor, Fg * D * G).div(4.0D * Math.abs(ns.dot(wo)));
        } else {
            // Transmission
            double eta = (ng.dot(wo) > 0) ? (1.0D / ior) : ior;
            Vector3D h = Vector3D.add(wo, wi).normalized();
            double Fg = fresnelDielectricExact(wo, h, ior);

            Vector3D hLocal = toLocal(h, ns).normalize();
            Vector3D woLocal = toLocal(wo, ns).normalize();
            Vector3D wiLocal = toLocal(wi, ns).normalize();

            double aspect = Math.sqrt(1.0D - 0.9D * anisotropic);
            double ax = Math.max(0.0001D, sqr(roughness) / aspect);
            double ay = Math.max(0.0001D, sqr(roughness) * aspect);

            double D = 1.0D / (Math.PI * ax * ay * Math.pow(
                    (hLocal.x * hLocal.x) / (ax * ax) +
                            (hLocal.y * hLocal.y) / (ay * ay) +
                            (hLocal.z * hLocal.z),
                    2));

            double lambdaL = (Math.sqrt(1.0 + (sqr(wiLocal.x * ax) + sqr(wiLocal.y * ay)) / sqr(wiLocal.z)) - 1.0D)
                    / 2.0D;
            double lambdaV = (Math.sqrt(1.0 + (sqr(woLocal.x * ax) + sqr(woLocal.y * ay)) / sqr(woLocal.z)) - 1.0D)
                    / 2.0D;
            double GL = 1.0D / (1.0D + lambdaL);
            double GV = 1.0D / (1.0D + lambdaV);
            double G = GL * GV;
            Vector3D sqrtBaseColor = new Vector3D(
                    Math.sqrt(baseColor.x),
                    Math.sqrt(baseColor.y),
                    Math.sqrt(baseColor.z));
            return Vector3D.mult(sqrtBaseColor, (1.0D - Fg) * D * G * Math.abs(h.dot(wi) * h.dot(wo)))
                    .div(Math.abs(ns.dot(wo)) * sqr(h.dot(wo) + eta * h.dot(wi)));
        }
    }

    @Override
    public double pdf(Vector3D wo, Vector3D wi) {
        double eta = (ng.dot(wo) > 0) ? (1.0D / ior) : ior;

        Vector3D h = Vector3D.add(wo, wi).normalized();
        if (ng.dot(wi) * ng.dot(wo) > 0) {
            // Reflection
            Vector3D hLocal = toLocal(h, ns).normalize();
            Vector3D woLocal = toLocal(wo, ns).normalize();

            double aspect = Math.sqrt(1.0D - 0.9D * anisotropic);
            double ax = Math.max(0.0001D, sqr(roughness) / aspect);
            double ay = Math.max(0.0001D, sqr(roughness) * aspect);

            double D = 1.0D / (Math.PI * ax * ay * Math.pow(
                    (hLocal.x * hLocal.x) / (ax * ax) +
                            (hLocal.y * hLocal.y) / (ay * ay) +
                            (hLocal.z * hLocal.z),
                    2));

            double lambdaV = (Math.sqrt(1.0 + (sqr(woLocal.x * ax) + sqr(woLocal.y * ay)) / sqr(woLocal.z)) - 1.0D)
                    / 2.0D;
            double GV = 1.0D / (1.0D + lambdaV);
            double pdfM = (D * GV * Math.abs(hLocal.z)) / Math.abs(woLocal.z);
            double Fr = fresnelDielectricExact(wo, h, ior);
            return Fr * pdfM / (4.0D * Math.abs(h.dot(wo)));
        }
        // Transmission
        Vector3D hLocal = toLocal(h, ns).normalize();
        Vector3D woLocal = toLocal(wo, ns).normalize();
        double aspect = Math.sqrt(1.0D - 0.9D * anisotropic);
        double ax = Math.max(0.0001D, sqr(roughness) / aspect);
        double ay = Math.max(0.0001D, sqr(roughness) * aspect);

        double D = 1.0D / (Math.PI * ax * ay * Math.pow(
                (hLocal.x * hLocal.x) / (ax * ax) +
                        (hLocal.y * hLocal.y) / (ay * ay) +
                        (hLocal.z * hLocal.z),
                2));

        double lambdaV = (Math.sqrt(1.0 + (sqr(woLocal.x * ax) + sqr(woLocal.y * ay)) / sqr(woLocal.z)) - 1.0D)
                / 2.0D;
        double GV = 1.0D / (1.0D + lambdaV);
        double Fr = fresnelDielectricExact(wo, h, ior);
        double denom = sqr((wo.dot(h) + eta * wi.dot(h)));
        double jacobian = (eta * eta * Math.abs(wi.dot(h))) / denom;
        double pdfM = (D * GV * wo.dot(h)) / wo.z;

        return (1.0D - Fr) * pdfM * jacobian;
    }

    @Override
    public BxDFSample sample(Vector3D wo) {
        double Fr = fresnelDielectricExact(wo, ns, ior);
        Vector3D wi;
        if (MathUtils.random() < Fr) {
            // Reflect
            Vector3D randomNormal = toWorld(ns, sampleVndfGGX(wo, Math.max(sqr(roughness), 0.0001D))).normalize();
            wi = reflect(wo, randomNormal);
            return new BxDFSample(wi, eval(wo, wi), pdf(wo, wi), Event.REFLECT, sqr(roughness) <= 0.0001D);
        }
        Vector3D randomNormal = toWorld(ns, sampleVndfGGX(wo, Math.max(sqr(roughness), 0.0001D))).normalize();

        wi = refract(wo, randomNormal, ior);
        if (wi == null) {
            // Total internal reflection, reflect instead
            randomNormal = toWorld(ns, sampleVndfGGX(wo, Math.max(sqr(roughness), 0.0001D))).normalize();
            wi = reflect(wo, randomNormal);
            return new BxDFSample(wi, eval(wo, wi), 1.0, Event.REFLECT, sqr(roughness) <= 0.0001D);
        }
        return new BxDFSample(wi, eval(wo, wi), pdf(wo, wi), Event.TRANSMIT, sqr(roughness) <= 0.0001D);
    }
}
