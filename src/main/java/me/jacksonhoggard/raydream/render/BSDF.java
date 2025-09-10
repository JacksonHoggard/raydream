package me.jacksonhoggard.raydream.render;

import me.jacksonhoggard.raydream.util.MathUtils;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Matrix3D;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public class BSDF {

    public enum Event { REFLECT, TRANSMIT }
    public enum Lobe { DIFFUSE, SPECULAR, CLEARCOAT, TRANSMIT_SPECULAR }

    public record BSDFSample(
        Vector3D l,    // sampled direction (unit)
        Vector3D f,    // BSDF value for the sampled lobe at (wo, wi)
        double pdf,    // mixture PDF over all active lobes for this hemisphere
        Event event,   // event type (reflection or transmission)
        Lobe lobe,     // which lobe produced wi
        boolean delta, // true for perfect mirror/refraction
        double eta     // relative IOR (only meaningful for transmission)
    ) {}

    private record LobePick(
        double pDiffuse,
        double pSpecular,
        double pClearcoat,
        double pTransmission,
        double pReflection
    ) {}

    public static BSDFSample sample(
        Ray ray,
        Material material,
        Vector3D normalHit,
        Vector2D texCoord,
        Vector3D tangent,
        Vector3D bitangent
    ) {
        Vector3D albedo = material.getAlbedo(texCoord);

        Vector3D v = ray.direction().negated();

        Vector3D cdlin = mon2lin(albedo);
        double cdlum = cdlin.x * 0.2126D + cdlin.y * 0.7152D + cdlin.z * 0.0722D;
        Vector3D ctint = cdlum > 0.0D ? Vector3D.div(cdlin, cdlum) : new Vector3D(1); // Normalize luminance to isolate hue + saturation
        Vector3D cspec0 = mix(Vector3D.mult(material.getSpecular(), 0.8D).mult(mix(new Vector3D(1), ctint, material.getSpecularTint())), cdlin, material.getMetallic());

        double fr = MathUtils.dielectric(normalHit.dot(v), 1.0D, Math.max(1.0001D, material.getIndexOfRefraction()));

        // Compute per-lobe selection probabilities
        LobePick lobePick = computeLobeProbs(albedo, cspec0, material, Math.max(0.0D, normalHit.dot(v)));
        boolean reflect = MathUtils.random() < fr || material.getSpecularTransmission() <= 0.0D;
        Lobe lobe = reflect ? pickLobe(lobePick) : Lobe.TRANSMIT_SPECULAR;

        // Sample the chosen lobe
        Vector3D wi = new Vector3D();
        Vector3D fLobe = new Vector3D();
        boolean delta = false;
        double eta = 1.0D;

        switch(lobe) {
            case DIFFUSE:
                wi = MathUtils.sampleCosineHemisphere(normalHit, tangent, bitangent);
                fLobe = evalDiffuse(material, cdlin, normalHit, v, wi, tangent, bitangent);
                break;
            case SPECULAR:
                wi = sampleGGXReflectionVNDF(material, cdlin, normalHit, v, tangent, bitangent);
                fLobe = evalSpecular(material, cdlin, normalHit, v, wi, tangent, bitangent);
                delta = (material.getRoughness() == 0.0D);
                break;
            case CLEARCOAT:
                wi = sampleGTR1Reflection(material, cdlin, normalHit, v, tangent, bitangent);
                fLobe = evalClearcoat(material, cdlin, normalHit, v, wi, tangent, bitangent);
                break;
            case TRANSMIT_SPECULAR:
                SampleDir sampleTrans = sampleGGXTransmissionVNDF(material, cdlin, normalHit, v, tangent, bitangent);
                if(sampleTrans == null || sampleTrans.wi == null) {
                    // Fallback to reflection or return invalid sample
                    wi = sampleGGXReflectionVNDF(material, cdlin, normalHit, v, tangent, bitangent);
                    fLobe = evalSpecular(material, cdlin, normalHit, v, wi, tangent, bitangent);
                    delta = (material.getRoughness() == 0.0D);
                    lobe = Lobe.SPECULAR;
                    break;
                }
                wi = sampleTrans.wi;
                eta = sampleTrans.eta;
                fLobe = evalTransmission(material, cdlin, normalHit, v, wi, eta, tangent, bitangent);
                delta = (material.getRoughness() == 0.0D);
                break;
        }

        // PDF calculation

        double NdotL = Math.max(0.0D, normalHit.dot(wi));
        double NdotV = Math.max(0.0D, normalHit.dot(v));

        Vector3D h = Vector3D.add(wi, v).normalize();
        double NdotH = Math.max(0.0D, normalHit.dot(h));
        double VdotH = Math.max(0.0D, v.dot(h));

        // Diffuse
        double pdfDiffuse = Math.max(0.0D, NdotL) / Math.PI;

        double aspect = Math.sqrt(1.0D - material.getAnisotropic() * 0.9D);
        double ax = Math.max(0.001D, Math.pow(material.getRoughness(), 2) / aspect);
        double ay = Math.max(0.001D, Math.pow(material.getRoughness(), 2) * aspect);

        // Specular reflection
        double ds = gtr2Aniso(NdotH, tangent.dot(h), bitangent.dot(h), ax, ay);
        double g1v = smithGGGXAniso(NdotV, v.dot(tangent), v.dot(bitangent), ax, ay);
        double pdfSpec = (Math.abs(VdotH) > 1e-9) ? ds * Math.abs(NdotH) * g1v / (4.0D * Math.abs(VdotH)) : 0.0D;

        // Clearcoat
        double dr = gtr1(NdotH, mix(0.1D, 0.001D, material.getClearcoatGloss()));
        double pdfClear = (Math.abs(VdotH) > 1e-9) ? dr * Math.abs(NdotH) / (4.0D * Math.abs(VdotH)) : 0.0D;

        // Specular transmission
        Vector3D wm;
        double pdfTrans = 0.0D;
        if(!reflect) {
            // For transmission, compute proper half-vector
            wm = Vector3D.add(Vector3D.mult(wi, eta), v).normalize();
            double wiDotWm = Math.abs(wi.dot(wm));
            double woDotWm = Math.abs(v.dot(wm));
            double pdfM = ggxVndfPdfM(v, wm, normalHit, tangent, bitangent, ax, ay);
            double denom = wiDotWm + woDotWm / Math.max(1e-9, eta);
            double j = (denom > 1e-9) ? (wiDotWm / (denom * denom)) : 0.0D;
            pdfTrans = pdfM * j;
        }

        // Build the mixture PDF
        double pdfMix = 0.0D;
        if(reflect) {
            pdfMix += lobePick.pDiffuse * pdfDiffuse +
                lobePick.pSpecular * pdfSpec +
                lobePick.pClearcoat * pdfClear;
        } else {
            pdfMix += lobePick.pTransmission * pdfTrans;
        }

        // Package result
        BSDFSample s = new BSDFSample(
            wi,
            fLobe,
            pdfMix,
            reflect ? Event.REFLECT : Event.TRANSMIT,
            lobe,
            delta,
            eta
        );
        return s;
    }

    public static double pdf(
        Material material,
        Vector3D albedo,
        Vector3D v,
        Vector3D l,
        boolean thin,
        Vector3D normal,
        Vector3D tangent,
        Vector3D bitangent
    ) {
        Vector3D cdlin = mon2lin(albedo);
        double cdlum = cdlin.x * 0.2126D + cdlin.y * 0.7152D + cdlin.z * 0.0722D;
        Vector3D ctint = cdlum > 0.0D ? Vector3D.div(cdlin, cdlum) : new Vector3D(1); // Normalize luminance to isolate hue + saturation
        Vector3D cspec0 = mix(Vector3D.mult(material.getSpecular(), 0.8D).mult(mix(new Vector3D(1), ctint, material.getSpecularTint())), cdlin, material.getMetallic());
        boolean refl = isInSameHemisphere(v, l, normal);
        LobePick lobePick = computeLobeProbs(albedo, cspec0, material, Math.max(0.0D, normal.dot(v)));

        if(refl) {
            // per-lobe directional PDFs
            double pdfDiff = Math.max(0.0D, normal.dot(l)) / Math.PI;

            Vector3D h = Vector3D.add(l, v).normalize();
            double NdotH = Math.max(0.0D, normal.dot(h));
            double VdotH = Math.max(0.0D, v.dot(h));
            double NdotV = Math.max(0.0D, normal.dot(v));

            // GGX reflection (VNDF)
            double aspect = Math.sqrt(Math.max(0.0, 1.0D - 0.9D * material.getAnisotropic()));
            double ax = Math.max(0.001D, Math.pow(material.getRoughness(), 2) / aspect);
            double ay = Math.max(0.001D, Math.pow(material.getRoughness(), 2) * aspect);
            double d = gtr2Aniso(NdotH, h.dot(tangent), h.dot(bitangent), ax, ay);
            double g1v = smithGGGXAniso(NdotV, v.dot(tangent), v.dot(bitangent), ax, ay);
            double pdfSpec = (Math.abs(VdotH) > 1e-9) ?
                d * Math.abs(NdotH) * g1v / (4.0D * Math.abs(VdotH)) : 0.0D;

            // Clearcoat
            double ac = mix(0.1D, 0.001D, material.getClearcoatGloss());
            double dc = gtr1(NdotH, ac);
            double pdfClear = dc * Math.abs(NdotH) / Math.max(1e-9, 4.0D * Math.abs(VdotH));

            // Mixture on reflection side
            return lobePick.pDiffuse * pdfDiff + lobePick.pSpecular * pdfSpec + lobePick.pClearcoat * pdfClear;
        } else {
            if((1.0D - material.getMetallic()) * material.getSpecularTransmission() <= 0.0D)
                return 0.0D;

            // Transmission
            double aspect = Math.sqrt(Math.max(0.0, 1.0D - 0.9D * material.getAnisotropic()));
            double ax = Math.max(0.001D, Math.pow(material.getRoughness(), 2) / aspect);
            double ay = Math.max(0.001D, Math.pow(material.getRoughness(), 2) * aspect);

            // Determine relative IOR based on which side of the surface we're on
            double etaOutside = 1.0D;
            double etaInside = Math.max(1.0001D, material.getIndexOfRefraction());
            // If view direction is in same hemisphere as normal, we're entering material
            double eta = (normal.dot(v) > 0.0D) ? (etaOutside / etaInside) : (etaInside / etaOutside);

            Vector3D wm = Vector3D.mult(l, eta).add(v).normalize();
            double pdfM = ggxVndfPdfM(v, wm, normal, tangent, bitangent, ax, ay);
            double wiDotM = Math.abs(l.dot(wm));
            double woDotM = Math.abs(v.dot(wm));
            double denom = wiDotM + woDotM/eta;
            double j = (denom > 1e-9) ? (wiDotM / (denom * denom)) : 0.0D;
            double pdfTransmit = pdfM * j;

            return lobePick.pTransmission * pdfTransmit;
        }
    }

    public static Vector3D eval(
        Material material,
        Vector3D albedo,
        Vector3D v,
        Vector3D l,
        boolean thin,
        Vector3D normal,
        Vector3D tangent,
        Vector3D bitangent
    ) {
        double NdotV = Math.max(0, normal.dot(v));
        double NdotL = Math.max(0, normal.dot(l));

        Vector3D h = Vector3D.add(l, v).normalize();
        double NdotH = Math.max(0.0D, normal.dot(h));
        double LdotH = Math.max(0.0D, l.dot(h));

        double TdotH = tangent.dot(h);
        double BdotH = bitangent.dot(h);

        double aspect = Math.sqrt(Math.max(0.0D, 1.0D - material.getAnisotropic() * 0.9D));
        double ax = Math.max(0.001D, Math.pow(material.getRoughness(), 2) / aspect);
        double ay = Math.max(0.001D, Math.pow(material.getRoughness(), 2) * aspect);

        Vector3D cdlin = mon2lin(albedo);
        double cdlum = cdlin.x * 0.2126D + cdlin.y * 0.7152D + cdlin.z * 0.0722D;
        Vector3D ctint = cdlum > 0.0D ? Vector3D.div(cdlin, cdlum) : new Vector3D(1); // Normalize luminance to isolate hue + saturation
        Vector3D cspec0 = mix(Vector3D.mult(material.getSpecular(), 0.8D).mult(mix(new Vector3D(1), ctint, material.getSpecularTint())), cdlin, material.getMetallic());
    
        Vector3D f0Diel = mix(new Vector3D(0.0D), new Vector3D(Vector3D.mult(material.getSpecular(), 0.08D)), 1.0D);
        f0Diel = mix(f0Diel, Vector3D.mult(f0Diel, ctint), material.getSpecularTint());

        Vector3D f = new Vector3D();
        
        // Determine if this is reflection or transmission based on hemisphere
        boolean isReflection = isInSameHemisphere(v, l, normal);
        
        if(isReflection) {
            // Reflection Lobes - evaluate all reflection components
            // Diffuse
            double fL = schlickFresnel(NdotL);
            double fV = schlickFresnel(NdotV);
            double fd90 = 0.5D + 2.0D * LdotH*LdotH * material.getRoughness();
            double fd = mix(1.0D, fd90, fL) * mix(1.0D, fd90, fV);

            double fss90 = LdotH * LdotH * material.getRoughness();
            double fss = mix(1.0D, fss90, fL) * mix(1.0D, fss90, fV);
            double ss = 1.25D * (fss * (1.0D / (NdotL + NdotV) - 0.5D) + 0.5D);

            Vector3D fDiffuse = Vector3D.mult(
                albedo,
                (1.0D - material.getSpecularTransmission()) * (1.0D - material.getMetallic()) * (mix(fd, ss, 0.0D)) * (1.0D/Math.PI)
            );

            // Main specular
            double dx = gtr2Aniso(NdotH, TdotH, BdotH, ax, ay);
            double fh = schlickFresnel(LdotH);
            Vector3D fs = mix(cspec0, new Vector3D(1), fh);
            double g = smithGGGXAniso(NdotL, l.dot(tangent), l.dot(bitangent), ax, ay) * smithGGGXAniso(NdotV, v.dot(tangent), v.dot(bitangent), ax, ay);
            Vector3D fSpecular = Vector3D.mult(
                dx * g,
                Vector3D.div(fs, Math.max(1e-6, 4.0D * NdotL * NdotV))
            );
            fSpecular.mult(1.0D - material.getSpecularTransmission() * (1.0D - material.getMetallic()));

            // Clearcoat
            double ac = mix(0.1D, 0.001D, material.getClearcoatGloss());
            double dc = gtr1(NdotH, ac);
            double gc = smithGGGX(NdotV, 0.25D) * smithGGGX(NdotL, 0.25D);
            double fc = mix(0.04D, 1.0D, schlickFresnel(LdotH));
            Vector3D fClearcoat = new Vector3D(0.25 * material.getClearcoat() * (dc * gc * fc / Math.max(1e-9, 4.0 * NdotL * NdotV)));

            // Sheen
            Vector3D cSheen = mix(Vector3D.ONE, ctint, material.getSheenTint());
            Vector3D fSheen = Vector3D.mult(cSheen, schlickFresnel(LdotH)).mult(1.0D - material.getMetallic());

            f = Vector3D.add(fDiffuse, fSpecular).add(fClearcoat).add(fSheen);
        }
        else {
            // Transmission - only evaluate if transmission is enabled and not fully metallic
            if(material.getSpecularTransmission() > 0.0D && material.getMetallic() < 1.0D) {
                // generalized half vector for transmission
                // Determine relative IOR based on which side of the surface we're on
                double etaOutside = 1.0D;
                double etaInside = Math.max(1.0001D, material.getIndexOfRefraction());
                // If view direction is in same hemisphere as normal, we're entering material
                double eta = (normal.dot(v) > 0.0D) ? (etaOutside / etaInside) : (etaInside / etaOutside);
                Vector3D wm = Vector3D.mult(l, eta).add(v).normalize();
                double NdotM = Math.max(0.0D, normal.dot(wm));
                double dm = gtr2Aniso(NdotM, wm.dot(tangent), wm.dot(bitangent), ax, ay);
                double g = smithGGGXAniso(Math.abs(NdotL), l.dot(tangent), l.dot(bitangent), ax, ay) * smithGGGXAniso(Math.abs(NdotV), v.dot(tangent), v.dot(bitangent), ax, ay);
                double wiDotM = Math.abs(l.dot(wm));
                double woDotM = Math.abs(v.dot(wm));

                // Fresnel term for transmission
                double ftr = 1.0D - MathUtils.dielectric(woDotM, 1.0D, Math.max(1.0001D, material.getIndexOfRefraction()));
                double denom = Math.pow(wiDotM + woDotM/eta, 2.0D);
                double scale = (eta*eta) * wiDotM * woDotM / Math.max(1e-9, denom);

                // Thin-tint
                Vector3D Ttint = thin ? new Vector3D(
                    Math.sqrt(cdlin.x),
                    Math.sqrt(cdlin.y),
                    Math.sqrt(cdlin.z)
                ) : new Vector3D(cdlin);
                
                // BTDF
                f = Vector3D.mult(
                    Ttint,
                    (1.0D - material.getMetallic()) * material.getSpecularTransmission() * ftr
                ).mult(
                    g * dm * scale
                ).div(
                    Math.max(1e-9, Math.abs(NdotL) * Math.abs(NdotV))
                );
            }
        }

        return f;

    }

    private static Vector3D evalDiffuse(
        Material material,
        Vector3D albedo,
        Vector3D normal,
        Vector3D viewDir,
        Vector3D lightDir,
        Vector3D tangent,
        Vector3D bitangent
    ) {
        double NdotL = Math.max(0.0D, normal.dot(lightDir));
        double NdotV = Math.max(0.0D, normal.dot(viewDir));
        if(NdotL <= 0.0D || NdotV <= 0.0D)
            return new Vector3D(0.0D);
        
        Vector3D h = Vector3D.add(lightDir, viewDir).normalize();
        double LdotH = Math.max(0.0D, lightDir.dot(h));

        Vector3D fLambert = Vector3D.div(albedo, Math.PI);

        // Diffuse fresnel
        double fL = schlickFresnel(NdotL);
        double fV = schlickFresnel(NdotV);
        double fd90 = 0.5D + 2.0D * LdotH*LdotH * material.getRoughness();
        double fd = mix(1.0D, fd90, fL) * mix(1.0D, fd90, fV);
        Vector3D fBaseDiffuse = Vector3D.mult(fLambert, fd);

        // Hanran-Krueger BRDF approximation of isotropic BSSRDF
        double fss90 = LdotH * LdotH * material.getRoughness();
        double fss = mix(1.0D, fss90, fL) * mix(1.0D, fss90, fV);
        double ss = 1.25D * (fss * (1.0D / (NdotL + NdotV) - 0.5D) + 0.5D);
        Vector3D fSubsurface = Vector3D.mult(fLambert, ss);

        Vector3D fDiffuse = Vector3D.add(
            Vector3D.mult((1.0D - material.getSubsurface()), fBaseDiffuse),
            Vector3D.mult(material.getSubsurface(), fSubsurface)
        );

        return fDiffuse;
    }

    private static Vector3D evalSpecular(
        Material material,
        Vector3D albedo,
        Vector3D normal,
        Vector3D viewDir,
        Vector3D lightDir,
        Vector3D tangent,
        Vector3D bitangent
    ) {
        double NdotL = Math.max(0.0D, normal.dot(lightDir));
        double NdotV = Math.max(0.0D, normal.dot(viewDir));
        if(NdotL <= 0.0D || NdotV <= 0.0D)
            return new Vector3D(0.0D);
        
        Vector3D h = Vector3D.add(lightDir, viewDir).normalize();
        double NdotH = Math.max(0.0D, normal.dot(h));
        double LdotH = Math.max(0.0D, lightDir.dot(h));

        double TdotH = tangent.dot(h);
        double BdotH = bitangent.dot(h);

        Vector3D cdlin = mon2lin(albedo);
        double cdlum = cdlin.x * 0.2126D + cdlin.y * 0.7152D + cdlin.z * 0.0722D;
        Vector3D ctint = cdlum > 0.0D ? Vector3D.div(cdlin, cdlum) : new Vector3D(1); // Normalize luminance to isolate hue + saturation
        Vector3D cspec0 = mix(Vector3D.mult(material.getSpecular(), 0.8D).mult(mix(new Vector3D(1), ctint, material.getSpecularTint())), cdlin, material.getMetallic());
        
        double aspect = Math.sqrt(1.0D - material.getAnisotropic() * 0.9D);
        double ax = Math.max(0.001D, Math.pow(material.getRoughness(), 2) / aspect);
        double ay = Math.max(0.001D, Math.pow(material.getRoughness(), 2) * aspect);
        double d = gtr2Aniso(NdotH, TdotH, BdotH, ax, ay);
        double fh = schlickFresnel(LdotH);
        Vector3D f = mix(cspec0, new Vector3D(1), fh);
        double g = smithGGGXAniso(NdotL, lightDir.dot(tangent), lightDir.dot(bitangent), ax, ay) * smithGGGXAniso(NdotV, viewDir.dot(tangent), viewDir.dot(bitangent), ax, ay);
        return Vector3D.div(
            Vector3D.mult(d * g, f),
            Math.max(1e-6, 4.0D * NdotL * NdotV)
        );
    }

    private static Vector3D evalClearcoat(
        Material material,
        Vector3D albedo,
        Vector3D normal,
        Vector3D viewDir,
        Vector3D lightDir,
        Vector3D tangent,
        Vector3D bitangent
    ) {
        double NdotL = Math.max(0.0D, normal.dot(lightDir));
        double NdotV = Math.max(0.0D, normal.dot(viewDir));
        if(NdotL <= 0.0D || NdotV <= 0.0D)
            return new Vector3D(0.0D);
        
        Vector3D h = Vector3D.add(lightDir, viewDir).normalize();
        double NdotH = Math.max(0.0D, normal.dot(h));
        double LdotH = Math.max(0.0D, lightDir.dot(h));

        double fh = schlickFresnel(LdotH);
        double d = gtr1(NdotH, mix(0.1D, 0.001D, material.getClearcoatGloss()));
        double f = mix(0.04D, 1.0D, fh);
        double g = smithGGGX(NdotL, 0.25D) * smithGGGX(NdotV, 0.25D);

        Vector3D fClearcoat = new Vector3D(
            d * g * f /
            Math.max(1e-6, 4.0D * NdotL * NdotV)
        );

        return fClearcoat;
    }

    private static Vector3D evalTransmission(
        Material material,
        Vector3D albedo,
        Vector3D normal,
        Vector3D viewDir,
        Vector3D lightDir,
        double eta,
        Vector3D tangent,
        Vector3D bitangent
    ) {
        double NdotL = normal.dot(lightDir);
        double NdotV = normal.dot(viewDir);
        if(NdotL * NdotV >= 0.0D) {
            return new Vector3D(0.0D);
        }
        Vector3D tint = new Vector3D(
            Math.sqrt(albedo.x),
            Math.sqrt(albedo.y),
            Math.sqrt(albedo.z)
        );
        
        Vector3D wm = Vector3D.add(Vector3D.mult(lightDir, eta), viewDir).normalize();
        double NdotM = normal.dot(wm);
        double LdotM = Math.abs(lightDir.dot(wm));
        double VdotM = Math.abs(viewDir.dot(wm));

        double aspect = Math.sqrt(1.0D - material.getAnisotropic() * 0.9D);
        double ax = Math.max(0.001D, Math.pow(material.getRoughness(), 2) / aspect);
        double ay = Math.max(0.001D, Math.pow(material.getRoughness(), 2) * aspect);
        double d = gtr2Aniso(NdotM, tangent.dot(wm), bitangent.dot(wm), ax, ay);
        double g = smithGGGXAniso(Math.abs(NdotL), lightDir.dot(tangent), lightDir.dot(bitangent), ax, ay)
                    * smithGGGXAniso(Math.abs(NdotV), viewDir.dot(tangent), viewDir.dot(bitangent), ax, ay);
        
        double ft = 1.0D - MathUtils.dielectric(VdotM, 1.0D, Math.max(1.0001D, material.getIndexOfRefraction()));

        double denom = (LdotM + VdotM/Math.max(1e-9, eta));
        double xi = (denom > 0.0D) ? (LdotM * VdotM) / (denom * denom) : 0.0D;
        double scale = (eta * eta);
        return Vector3D.mult(
            tint,
            d * g * ft * scale * xi
        ).div(
            Math.max(1e-6, (Math.abs(NdotL) * Math.abs(NdotV)))
        );
    }

    private static Lobe pickLobe(LobePick lobePick) {
        Lobe pick = null;
        do {
            double r = MathUtils.random();
            if(r < lobePick.pDiffuse) {
                pick = Lobe.DIFFUSE;
            } else if(r < lobePick.pDiffuse + lobePick.pSpecular) {
                pick = Lobe.SPECULAR;
            } else if(r < lobePick.pDiffuse + lobePick.pSpecular + lobePick.pClearcoat) {
                pick = Lobe.CLEARCOAT;
            } else if(r < lobePick.pDiffuse + lobePick.pSpecular + lobePick.pClearcoat + lobePick.pTransmission) {
                pick = Lobe.TRANSMIT_SPECULAR;
            }
        } while(pick == null);

        return pick;
    }

    /**
     * Computes the probabilities for each lobe (diffuse, specular, clearcoat, transmission, reflection).
     * @param baseColor The base color of the material.
     * @param cspec0 The specular color of the material.
     * @param material The material properties.
     * @param NdotV The dot product of the normal and view direction.
     * @return two LobePick objects for reflection and transmission
     */
    private static LobePick computeLobeProbs(
        Vector3D baseColor, Vector3D cspec0,
        Material material, double NdotV
    ) {
        // 1) Scalars for weights
        double cSpecLum = 0.3 * cspec0.x + 0.6 * cspec0.y + 0.1 * cspec0.z;
        double f0Spec = Math.min(0.999D, Math.max(0.0, cSpecLum));
        double fVSpec = MathUtils.schlickFresnel(f0Spec, Math.abs(NdotV));
        // Transmission tint strength (thin approximation if the material is thin)
        Vector3D transTint = new Vector3D(baseColor);
        if(material.isThin()) {
            transTint.set(
                Math.sqrt(transTint.x),
                Math.sqrt(transTint.y),
                Math.sqrt(transTint.z)
            );
        }

        // 2) Energy heuristics (unnormalized)
        double wDiffuse = (1.0D - material.getMetallic()) * (1.0D - material.getSpecularTransmission());
        double wSpecular = Math.max(1e-4, fVSpec);
        double wClearcoat = (0.25D * material.getClearcoat());

        // 3) Mixture reflection and transmission
        // Reflection
        double totalWeight = wDiffuse + wSpecular + wClearcoat;

        double fr = MathUtils.dielectric(NdotV, 1.0D, Math.max(1.0001D, material.getIndexOfRefraction()));
        double ft = 1.0D - fr;
        LobePick reflection;
        if(totalWeight > 0.0D) {
            reflection = new LobePick(
                wDiffuse / totalWeight,
                wSpecular / totalWeight,
                wClearcoat / totalWeight,
                ft,
                fr
            );
        } else {
            reflection = new LobePick(
                1.0D,
                0.0D,
                0.0D,
                ft,
                fr
            );
        }

        return reflection;
    }

    private static boolean isInSameHemisphere(Vector3D v1, Vector3D v2, Vector3D normal) {
        return Math.signum(v1.dot(normal)) == Math.signum(v2.dot(normal));
    }

    private static Vector3D sampleGGXReflectionVNDF(
        Material material,
        Vector3D albedo,
        Vector3D normal,
        Vector3D viewDir,
        Vector3D tangent,
        Vector3D bitangent
    ) {
        // Only sample reflection if wo is in the top hemisphere
        double NdotV = normal.dot(viewDir);
        if(NdotV <= 0.0D) {
            return new Vector3D();
        }

        // Roughness -> anisotropic alphas
        double aspect = Math.sqrt(Math.max(0.0, 1.0 - 0.9 * material.getAnisotropic()));
        double alpha = Math.max(1e-4, material.getRoughness() * material.getRoughness());
        double ax = Math.max(1e-4, alpha / aspect);
        double ay = Math.max(1e-4, alpha * aspect);

        // Localize wo
        Matrix3D TBN = new Matrix3D(new double[] {
            tangent.x, bitangent.x, normal.x,
            tangent.y, bitangent.y, normal.y,
            tangent.z, bitangent.z, normal.z
        });
        Vector3D v = viewDir.mult(TBN.transpose()).normalize();

        // Heitz visible-normal sampling (anisotropic)
        // Stretch view
        Vector3D vh = new Vector3D(ax * v.x, ay * v.y, v.z).normalize();

        // Orthonormal basis around Vh
        Vector3D T1, T2;
        if (vh.z < 0.9999D) {
            T1 = vh.cross(new Vector3D(0, 0, 1)).normalize();
            T2 = vh.cross(T1);
        } else {
            T1 = new Vector3D(1, 0, 0);
            T2 = new Vector3D(0, 1, 0);
        }

        // Sample a point on the projected area (unit disk), then evaluate
        double u1 = Math.random();
        double u2 = Math.random();
        double r = Math.sqrt(u1);
        double phi = 2.0D * Math.PI * u2;
        double t1 = r * Math.cos(phi);
        double t2 = r * Math.sin(phi);

        // Heitz' bias for correct VNDF
        double s = 0.5D * (1.0D + vh.z);
        t2 = (1.0D - s) * Math.sqrt(Math.max(0.0D, 1.0D - t1*t1)) + s * t2;

        // Compute microfacet normal in stretched space
        double t1sq = t1 * t1;
        double t2sq = t2 * t2;
        double z = Math.sqrt(Math.max(0.0D, 1.0D - t1sq - t2sq));
        Vector3D nh = Vector3D.add(
            Vector3D.add(T1.mult(t1), T2.mult(t2)),
            Vector3D.mult(vh, z)
        ).normalize();
        
        // Unstretch back to anisotropic space -> microfacet normal m
        Vector3D m = new Vector3D(ax * nh.x, ay * nh.y, Math.max(0.0D, nh.z)).normalize();
        if(m.z <= 0.0D) {
            return new Vector3D();
        }

        // Reflect wo about m
        Vector3D wiLocal = MathUtils.reflect(v.negated(), m); // incident = -wo
        if(wiLocal.z <= 0.0D) {
            return new Vector3D();
        }

        // Transform wi back to world space
        return wiLocal.mult(TBN).normalize();
    }

    private static Vector3D sampleGTR1Reflection(
        Material material,
        Vector3D albedo,
        Vector3D normal,
        Vector3D viewDir,
        Vector3D tangent,
        Vector3D bitangent
    ) {
        // Only sample reflection if wo is in the top hemisphere
        double NdotV = normal.dot(viewDir);
        if(NdotV <= 0.0D) {
            return new Vector3D();
        }

        // Roughness
        double alpha = (1.0D - material.getClearcoatGloss()) * 0.1D + material.getClearcoatGloss() * 0.001D;
        alpha = Math.clamp(alpha, 1e-6, 0.999D);

        // Localize wo
        Matrix3D TBN = new Matrix3D(new double[] {
            tangent.x, bitangent.x, normal.x,
            tangent.y, bitangent.y, normal.y,
            tangent.z, bitangent.z, normal.z
        });
        Vector3D v = viewDir.mult(TBN.transpose()).normalize();

        // Sample GTR1 (isotropic)
        double u1 = Math.random();
        double u2 = Math.random();
        double a2 = alpha * alpha;
        double phi = 2.0D * Math.PI * u1;
        double cosTheta = Math.sqrt((1.0D - Math.pow(a2, 1.0D - u2)) / (1.0D - a2));
        cosTheta = Math.min(1.0D, Math.max(0.0D, cosTheta));
        double sinTheta = Math.sqrt(Math.max(0.0D, 1.0D - cosTheta * cosTheta));

        // Microfacet normal m in local space
        Vector3D m = new Vector3D(
            sinTheta * Math.cos(phi),
            sinTheta * Math.sin(phi),
            cosTheta
        );

        // Reflect wo about m
        Vector3D wiLocal = MathUtils.reflect(v.negated(), m); // incident = -wo
        if(wiLocal.z <= 0.0D) {
            return new Vector3D();
        }

        // Transform wi back to world space
        return wiLocal.mult(TBN).normalize();
    }

    private record SampleDir(
        Vector3D wi,    // world-space transmitted/reflected direction
        double pdf,     // probability density function value
        double eta      // relative IOR used for refraction
    ) {}

    private static SampleDir sampleGGXTransmissionVNDF(
        Material material,
        Vector3D albedo,
        Vector3D normal,
        Vector3D viewDir,
        Vector3D tangent,
        Vector3D bitangent
    ) {
        // Anisotropic alphas
        double aspect = Math.sqrt(Math.max(0.0D, 1.0D - 0.9 * material.getAnisotropic()));
        double alpha = Math.max(1e-4, material.getRoughness() * material.getRoughness());
        double ax = Math.max(1e-4, alpha / aspect);
        double ay = Math.max(1e-4, alpha * aspect);

        // Localize
        Matrix3D TBN = new Matrix3D(new double[] {
            tangent.x, bitangent.x, normal.x,
            tangent.y, bitangent.y, normal.y,
            tangent.z, bitangent.z, normal.z
        });
        Vector3D v = viewDir.mult(TBN.transpose()).normalize();

        // Sample visible-normal GGX (Heitz), anisotropic
        // Stretch the view
        Vector3D vh = new Vector3D(ax * v.x, ay * v.y, v.z).normalize();

        // Orthonormal basis around vh
        Vector3D T1, T2;
        if(vh.z < 0.9999D) {
            T1 = vh.cross(new Vector3D(0, 0, 1)).normalize();
            T2 = vh.cross(T1);
        } else {
            T1 = new Vector3D(1, 0, 0);
            T2 = new Vector3D(0, 1, 0);
        }

        // Disk sample
        double u1 = Math.random();
        double u2 = Math.random();
        double r = Math.sqrt(u1);
        double phi = 2.0D * Math.PI * u2;
        double t1 = r * Math.cos(phi);
        double t2 = r * Math.sin(phi);

        // Heitz' bias for VNDF
        double s = 0.5D * (1.0D + vh.z);
        t2 = (1.0D - s) * Math.sqrt(Math.max(0.0D, 1.0D - t1*t1)) + s * t2;

        // Half vector in stretched space
        double z = Math.sqrt(Math.max(0.0, 1.0D - t1*t1 - t2*t2));
        Vector3D nh = Vector3D.add(
            Vector3D.add(T1.mult(t1), T2.mult(t2)),
            Vector3D.mult(vh, z)
        ).normalize();

        // Unstretch back -> microfacet normal
        Vector3D m = new Vector3D(ax * nh.x, ay * nh.y, Math.max(0.0D, nh.z)).normalize();

        // Ensure m faces the incident side defined by v
        if(v.dot(m) < 0.0D) {
            m.negate();
        }

        // Determine relative IOR (outside is 1.0, inside is material.getIndexOfRefraction())
        // If v.z > 0, we're in the outside medium (air), entering the material
        double etaOutside = 1.0D;
        double etaInside = Math.max(1.0001D, material.getIndexOfRefraction());
        double eta = v.z > 0.0D ? (etaOutside / etaInside) : (etaInside / etaOutside);

        // Refract -v across m with Snell
        Vector3D wiLocal = new Vector3D();
        if(!MathUtils.refractThroughMicrofacet(v, m, eta, wiLocal)) {
            // Total internal reflection occurred
            return new SampleDir(null, 0.0D, 1.0D);
        }

        // Directional PDF for transmission
        double pm = ggxVndfPdfM(v, m, normal, tangent, bitangent, ax, ay);
        double wiDotM = Math.abs(wiLocal.dot(m));
        double woDotM = Math.abs(v.dot(m));
        double denom = wiDotM + woDotM / Math.max(1e-9, eta);
        double j = (denom > 0.0D) ? (wiDotM / (denom * denom)) : 0.0D;

        double pdf = pm * j;

        // Back to world space & return
        return new SampleDir(wiLocal.mult(TBN).normalize(), pdf, eta);
    }

    private static double ggxVndfPdfM(Vector3D v, Vector3D wm, Vector3D normal, Vector3D tangent, Vector3D bitangent, double ax, double ay) {
        // reject wrong hemispheres
        double NzV = Math.abs(normal.dot(v));
        double NzM = Math.abs(normal.dot(wm));
        if(NzV <= 0.0D || NzM <= 0.0D) return 0.0D;

        double D = gtr2Aniso(normal.dot(wm), wm.dot(tangent), wm.dot(bitangent), ax, ay);
        double g1v = smithGGGXAniso(NzV, v.dot(tangent), v.dot(bitangent), ax, ay);

        double vDotM = Math.abs(v.dot(wm));

        return D * g1v * (vDotM / NzV);
    }

    private static double schlickFresnel(double u) {
        double m = Math.clamp(1 - u, 0, 1);
        double m2 = m * m;
        return m2 * m2 * m;
    }

    private static double gtr1(double NdotH, double a) {
        if (a >= 1.0D) return 1.0D / Math.PI;
        double a2 = a * a;
        double t = 1 + (a2 - 1) * NdotH * NdotH;
        return (a2 - 1) / (Math.PI * Math.log(a2) * t);
    }

    private static double gtr2Aniso(double NdotH, double HdotX, double HdotY, double ax, double ay) {
        return 1.0D / (Math.PI * ax*ay * Math.pow(Math.pow(HdotX / ax, 2) + Math.pow(HdotY / ay, 2) + NdotH*NdotH, 2));
    }

    private static double smithGGGX(double NdotV, double alphaG) {
        double a = alphaG * alphaG;
        double b = NdotV * NdotV;
        return 1.0D / (NdotV + Math.sqrt(a + b - a * b));
    }

    private static double smithGGGXAniso(double NdotV, double VdotX, double VdotY, double ax, double ay) {
        return 1.0D / (NdotV + Math.sqrt(Math.pow(VdotX * ax, 2) + Math.pow(VdotY * ay, 2) + NdotV*NdotV));
    }

    private static Vector3D mix(Vector3D a, Vector3D b, double t) {
        return Vector3D.add(Vector3D.mult(a, 1 - t), Vector3D.mult(b, t));
    }

    private static double mix(double a, double b, double t) {
        return a * (1 - t) + b * t;
    }

    private static Vector3D mon2lin(Vector3D in) {
        return new Vector3D(
            Math.pow(in.x, 2.2),
            Math.pow(in.y, 2.2),
            Math.pow(in.z, 2.2)
        );
    }
}
