package me.jacksonhoggard.raydream.render;

import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public class BSDF {

    public static void bsdf(
            Vector3D color,
            Ray ray,
            Object objectHit,
            Vector3D pointHit,
            Vector3D normalHit,
            Vector2D texCoord,
            Vector3D pointOnLight,
            Vector3D tangent,
            Vector3D bitangent
        )
    {
        Material material = objectHit.getMaterial();
        Vector3D albedo = material.getAlbedo(texCoord);

        Vector3D l = Vector3D.sub(pointHit, pointOnLight).normalized();
        Vector3D v = ray.direction().negated().normalized();

        double NdotL = normalHit.dot(l);
        double NdotV = normalHit.dot(v);

        Vector3D h = Vector3D.add(l, v).normalized();
        double NdotH = normalHit.dot(h);
        double LdotH = l.dot(h);
        double VdotH = v.dot(h);

        Vector3D cdlin = mon2lin(albedo);
        double cdlum = cdlin.x * 0.2126D + cdlin.y * 0.7152D + cdlin.z * 0.0722D;
        Vector3D ctint = cdlum > 0.0D ? Vector3D.div(cdlin, cdlum) : new Vector3D(1); // Normalize luminance to isolate hue + saturation
        Vector3D cspec0 = mix(Vector3D.mult(material.getSpecular(), 0.8D).mult(mix(new Vector3D(1), ctint, material.getSpecularTint())), cdlin, material.getMetallic());
        Vector3D csheen = mix(new Vector3D(1), ctint, material.getSheenTint());

        // Diffuse fresnel
        double fL = schlickFresnel(NdotL);
        double fV = schlickFresnel(NdotV);
        double fd90 = 0.5D + 2.0D * LdotH*LdotH * material.getRoughness();
        double fd = mix(1.0D, fd90, fL) * mix(1.0D, fd90, fV);

        // Hanran-Krueger BRDF approximation of isotropic BSSRDF
        double fss90 = LdotH * LdotH * material.getRoughness();
        double fss = mix(1.0D, fss90, fL) * mix(1.0D, fss90, fV);
        double ss = 1.25D * (fss * (1.0D / (NdotL + NdotV) - 0.5D) + 0.5D);

        // Specular
        double aspect = Math.sqrt(1.0D - material.getAnisotropic() * 0.9D);
        double ax = Math.max(0.001D, Math.pow(material.getRoughness(), 2) / aspect);
        double ay = Math.max(0.001D, Math.pow(material.getRoughness(), 2) * aspect);
        double ds = gtr2Aniso(NdotH, h.dot(tangent), h.dot(bitangent), ax, ay);
        double fh = schlickFresnel(LdotH);
        Vector3D fs = mix(cspec0, new Vector3D(1), fh);
        double gs = smithGGGXAniso(NdotL, l.dot(tangent), l.dot(bitangent), ax, ay) * smithGGGXAniso(NdotV, v.dot(tangent), v.dot(bitangent), ax, ay);

        // Sheen
        Vector3D fSheen = csheen.mult(material.getSheen()).mult(fh);

        // Clearcoat
        // ior = 1.5
        // f0 = 0.04
        double dr = gtr1(NdotH, mix(0.1D, 0.001D, material.getClearcoatGloss()));
        double fr = mix(0.04D, 1.0D, fh);
        double gr = smithGGGX(NdotL, 0.25D) * smithGGGX(NdotV, 0.25D);

        // Glass
        Vector3D fGlass = new Vector3D();
        Vector3D nh = Vector3D.mult(h, material.getIndexOfRefraction());
        double NHdotL = nh.dot(l);
        double NHdotV = nh.dot(v);
        double rs = (VdotH - NHdotL) / (VdotH + NHdotL);
        double rp = (NHdotV - LdotH) / (NHdotV + LdotH);
        double fg = 0.5D * (rs*rs + rp*rp);
        if(NdotV * NdotL > 0.0D) { // Reflection
            fGlass.set(
                Vector3D.mult(albedo, fg * ds * gs)
                        .div(4.0D * Math.abs(NdotV)));
        } else { // Refraction
            fGlass.set(new Vector3D(
                Math.sqrt(albedo.x),
                Math.sqrt(albedo.y),
                Math.sqrt(albedo.z)
            ))
            .mult((1.0D - fg) * ds * gs)
            .mult(Math.abs(LdotH * VdotH))
            .div(Math.abs(NdotV) * Math.pow(VdotH + NHdotL, 2));
        }

        if(NdotV <= 0.0D) {
            fd = 0.0D;
            fs = new Vector3D();
            fr = 0.0D;
            fSheen = new Vector3D(0.0D);
        }

        color.add(
            Vector3D.add(fSheen, Vector3D.mult(cdlin, mix(fd, ss, material.getSubsurface())).mult(1.0D/Math.PI).mult(1.0D - material.getSpecularTransmission())) // Diffuse
            .mult(1.0D - material.getMetallic())
            .add(Vector3D.mult(fs, gs * ds).mult(1.0D - material.getSpecularTransmission() * (1.0D - material.getMetallic()))) // Specular
            .add(new Vector3D(1).mult(0.25D * material.getClearcoat() * gr * fr * dr)) // Clearcoat
            .add(Vector3D.mult(fGlass, material.getSpecularTransmission() * (1.0D - material.getMetallic()))) // Glass
        );

        // Final BSDF
        // Vector3D fFinal = Vector3D.mult((1.0D - material.getSpecularTransmission()) * (1.0D - material.getMetallic()), fDiffuse)
        //                         .add(Vector3D.mult((1.0D - material.getMetallic()) * material.getSheen(), fSheen))
        //                         .add(Vector3D.mult(1.0D - material.getSpecularTransmission() * (1.0D - material.getMetallic()), fMetal))
        //                         .add(new Vector3D((0.25D * material.getClearcoat()) * fClearcoat))
        //                         .add(Vector3D.mult((1.0D - material.getMetallic()) * material.getSpecularTransmission(), fGlass));

        // // Sheen
        // double luminance = 0.2126D * albedo.x + 0.7152D * albedo.y + 0.0722D * albedo.z;
        // Vector3D cTint = luminance > 0.0D ? Vector3D.div(albedo, luminance) : new Vector3D(1.0D);
        // Vector3D cspec0 = mix(Vector3D.mult(material.getSpecular(), 0.8D).mult(mix(new Vector3D(1), cTint, material.getSpecularTint())), albedo, material.getMetallic());
        // Vector3D cSheen = new Vector3D(1.0D - material.getSheenTint()).add(Vector3D.mult(material.getSheenTint(), cTint));
        // Vector3D fSheen = Vector3D.mult(cSheen, Math.pow(1.0D - Math.abs(LdotH), 5)).mult(Math.abs(NdotL));

        // // Diffuse
        // double fd90 = 0.5D + 2.0D * material.getRoughness() * Math.pow(Math.abs(LdotH), 2);
        // double fdOut = (1.0D + (fd90 - 1.0D) * Math.pow((1.0D - Math.abs(NdotL)), 5));
        // double fdIn = (1.0D + (fd90 - 1.0D) * Math.pow((1.0D - Math.abs(NdotV)), 5));
        // Vector3D fBaseDiffuse = Vector3D.div(albedo, Math.PI).mult(fdIn * fdOut).mult(Math.abs(NdotL));

        // // Subsurface
        // double fss90 = material.getRoughness() * Math.pow(Math.abs(LdotH), 2);
        // double fssIn = (1.0D + (fss90 - 1.0D) * Math.pow(1.0D - Math.abs(NdotV), 5));
        // double fssOut = (1.0D + (fss90 - 1.0D) * Math.pow(1.0D - Math.abs(NdotL), 5));
        // Vector3D fSubsurface = Vector3D.mult(albedo, 1.25D).div(Math.PI).mult(fssIn * fssOut * ((1.0D / (Math.abs(NdotL) + Math.abs(NdotV))) - 0.5D) + 0.5D).mult(Math.abs(NdotL));

        // Vector3D fDiffuse = Vector3D.mult(1.0D - material.getSubsurface(), fBaseDiffuse).add(Vector3D.mult(material.getSubsurface(), fSubsurface));

        // // Specular
        // double aspect = Math.sqrt(1.0D - material.getAnisotropic() * 0.9D);
        // double ax = Math.max(0.001D, Math.pow(material.getRoughness(), 2) / aspect);
        // double ay = Math.max(0.001D, Math.pow(material.getRoughness(), 2) * aspect);
        // double dm = gtr2Aniso(NdotH, h.dot(tangent), h.dot(bitangent), ax, ay);
        // double fh = schlickFresnel(LdotH);
        // Vector3D fm = mix(cspec0, new Vector3D(1), fh);
        // double gm = smithGGGXAniso(NdotL, l.dot(tangent), l.dot(bitangent), ax, ay) * smithGGGXAniso(NdotV, v.dot(tangent), v.dot(bitangent), ax, ay);

        // // Metal
        // Vector3D fMetal = Vector3D.mult(fm, dm * gm).div(4.0D * Math.abs(NdotV));

        // // Clearcoat
        // double r0 = ((1.5D - 1.0D) * (1.5D - 1.0D)) / ((1.5D + 1.0D) * (1.5D + 1.0D));
        // double fc = r0 + (1.0D - r0) * Math.pow(1.0D - Math.abs(LdotH), 5);
        // double ag = ((1.0D - material.getClearcoatGloss()) * 0.1D) + (material.getClearcoatGloss() * 0.001D);
        // double dc = ((ag*ag) - 1.0D) / (Math.PI * Math.log(ag*ag) * (1.0D + ((ag*ag) - 1.0D) * (NdotH*NdotH)));
        // double fClearcoat = (fc * dc * gm) / (4.0D * Math.abs(NdotV));

        


        // color.add(fFinal);
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
