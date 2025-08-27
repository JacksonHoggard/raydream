package me.jacksonhoggard.raydream.render;

import me.jacksonhoggard.raydream.object.Object;
import me.jacksonhoggard.raydream.light.Light;
import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public class BSDF {

    public static void brdf(
            Vector3D color,
            Ray ray,
            Object objectHit,
            Vector3D pointHit,
            Vector3D normalHit,
            Vector2D texCoord,
            Light light,
            Vector3D x,
            Vector3D y
        )
    {
        Material material = objectHit.getMaterial();
        Vector3D albedo = material.getAlbedo(texCoord);

        Vector3D l = Vector3D.sub(light.getPosition(), pointHit).normalized();
        Vector3D v = ray.direction().negated().normalized();

        double NdotL = normalHit.dot(l);
        double NdotV = normalHit.dot(v);
        if(NdotL < 0 || NdotV < 0) return;

        Vector3D h = Vector3D.add(l, v).normalized();
        double NdotH = normalHit.dot(h);
        double LdotH = l.dot(h);

        Vector3D cdlin = mon2lin(albedo);
        double cdlum = 0.3 * cdlin.x + 0.6 * cdlin.y + 0.1 * cdlin.z; // Luminance

        Vector3D ctint = cdlum > 0 ? Vector3D.div(cdlin, cdlum) : new Vector3D(1); // Normalize luminance to isolate hue + saturation
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
        double ax = Math.max(0.001, Math.pow(material.getRoughness(), 2) / aspect);
        double ay = Math.max(0.001, Math.pow(material.getRoughness(), 2) * aspect);
        double ds = gtr2Aniso(NdotH, h.dot(x), h.dot(y), ax, ay);
        double fh = schlickFresnel(LdotH);
        Vector3D fs = mix(cspec0, new Vector3D(1), fh);
        double gs = smithGGGXAniso(NdotL, l.dot(x), l.dot(y), ax, ay) * smithGGGXAniso(NdotV, v.dot(x), v.dot(y), ax, ay);

        // Sheen
        Vector3D fSheen = csheen.mult(material.getSheen()).mult(fh);

        // Clearcoat
        // ior = 1.5
        // f0 = 0.04
        double dr = gtr1(NdotH, mix(0.1D, 0.001D, material.getClearcoatGloss()));
        double fr = mix(0.04D, 1.0D, fh);
        double gr = smithGGGX(NdotL, 0.25D) * smithGGGX(NdotV, 0.25D);

        color.add(
            Vector3D.add(fSheen, Vector3D.mult(cdlin, mix(fd, ss, material.getSubsurface())).mult(1.0D/Math.PI)) // Diffuse
            .mult(1.0D - material.getMetallic())
            .add(Vector3D.mult(fs, gs * ds)) // Specular
            .add(new Vector3D(1).mult(0.25D * material.getClearcoat() * gr * fr * dr)) // Clearcoat
        );
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
