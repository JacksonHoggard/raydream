package me.jacksonhoggard.raydream.material.bxdf.disney;

import java.util.HashMap;

import me.jacksonhoggard.raydream.material.bxdf.BSDF;
import me.jacksonhoggard.raydream.material.bxdf.BxDF;
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
    boolean reflect = cosTheta(wi) * cosTheta(wo) > 0.0D;
    double etaP = 1.0D;
    if (!reflect)
      etaP = (cosTheta(wo) > 0.0D) ? ior : 1.0D / ior;
    Vector3D wm = Vector3D.mult(wi, etaP).add(wo);
    if(cosTheta(wi) == 0 || cosTheta(wo) == 0 || wm.dot(wm) == 0)
      return Vector3D.ZERO;
    MathUtils.faceForward(wm.normalize(), new Vector3D(0, 0, 1));
    if(wm.dot(wi) * cosTheta(wi) < 0 || wm.dot(wo) * cosTheta(wo) < 0)
      return Vector3D.ZERO;

    double aspect = Math.sqrt(1.0D - 0.9D * anisotropic);
    double ax = Math.max(0.0001D, sqr(roughness) / aspect);
    double ay = Math.max(0.0001D, sqr(roughness) * aspect);
    
    double F = fresnelDielectricExact(wo.dot(wm), ior);
    if(reflect) {
      return new Vector3D(pdfVndfGGX(wm, ax, ay) * G_GGX(wo, wi, ax, ay) * F / Math.abs(4.0D * cosTheta(wi) * cosTheta(wo)))
          .mult(baseColor);
    } else {
      double denom = sqr(wi.dot(wm) + wo.dot(wm) / etaP) * cosTheta(wi) * cosTheta(wo);
      double ft = pdfVndfGGX(wm, ax, ay) * (1.0D - F) * G_GGX(wo, wi, ax, ay)
          * Math.abs(wi.dot(wm) * wo.dot(wm) / denom);
      return new Vector3D(ft / sqr(etaP)).mult(baseColor);
    }
  }

  @Override
  public double pdf(Vector3D wo, Vector3D wi) {
    boolean reflect = cosTheta(wi) * cosTheta(wo) > 0.0D;
    double etaP = 1.0D;
    if (!reflect)
      etaP = (cosTheta(wo) > 0.0D) ? ior : 1.0D / ior;
    Vector3D wm = Vector3D.mult(wi, etaP).add(wo);
    if(cosTheta(wi) == 0 || cosTheta(wo) == 0 || wm.dot(wm) == 0)
      return 0.0D;
    MathUtils.faceForward(wm.normalize(), new Vector3D(0, 0, 1));
    if(wm.dot(wi) * cosTheta(wi) < 0 || wm.dot(wo) * cosTheta(wo) < 0)
      return 0.0D;
    double R = fresnelDielectricExact(wm.dot(wo), ior);
    double T = 1.0D - R;

    double aspect = Math.sqrt(1.0D - 0.9D * anisotropic);
    double ax = Math.max(0.0001D, sqr(roughness) / aspect);
    double ay = Math.max(0.0001D, sqr(roughness) * aspect);

    double pdf;
    if(reflect) {
      pdf = pdfVndfGGX(wm, ax, ay) / (4.0D * Math.abs(wo.dot(wm)) * R / (R + T));
    } else {
      double denom = sqr(wi.dot(wm) + wo.dot(wm) / etaP);
      double dwmDwi = Math.abs(wi.dot(wm)) / denom;
      pdf = pdfVndfGGX(wm, ax, ay) * dwmDwi * T / (R + T);
    }
    return pdf;
  }

  @Override
  public BxDFSample sample(Vector3D woWorld) {
    // Localize
    Vector3D wo = shadingFrame.toLocal(woWorld);
    double aspect = Math.sqrt(1.0D - 0.9D * anisotropic);
    double ax = Math.max(0.0001D, sqr(roughness) / aspect);
    double ay = Math.max(0.0001D, sqr(roughness) * aspect);
    // Sample microfacet normal
    Vector3D wm = sampleVndfGGXAniso(wo, ax, ay);
    double R = fresnelDielectricExact(wo.dot(wm), ior);
    double T = 1.0D - R;
    double pdf;
    if(MathUtils.random() < R / (R + T)) {
      Vector3D wi = reflect(wo, wm);
      if(!sameHemisphere(wo, wi))
        return new BxDFSample(Vector3D.ZERO, Vector3D.ZERO, 0.0D, null, false);
        pdf = pdfVndfGGX(wm, ax, ay) / (4.0D * Math.abs(wo.dot(wm))) * R / (R + T);
      Vector3D f = new Vector3D(pdf * G_GGX(wo, wi, ax, ay) * R / (4.0D * cosTheta(wi) * cosTheta(wo)))
          .mult(baseColor);
      return new BxDFSample(shadingFrame.toWorld(wi), f, pdf, BxDF.Event.REFLECT, sqr(roughness) <= 1e-4D);
    } else {
      Vector3D sqrtBaseColor = new Vector3D(
          Math.sqrt(baseColor.x),
          Math.sqrt(baseColor.y),
          Math.sqrt(baseColor.z)
      );
      Refraction refraction = refract(wo, wm, ior);
      if(sameHemisphere(wo, refraction.wt()) || refraction.wt().z == 0.0D || refraction.tir())
        return new BxDFSample(Vector3D.ZERO, Vector3D.ZERO, 0.0D, null, false);
      double denom = sqr(refraction.wt().dot(wm) + wo.dot(wm) / refraction.eta());
      double dwmDwi = Math.abs(refraction.wt().dot(wm)) / denom;
      pdf = pdfVndfGGX(wm, ax, ay) * dwmDwi * T / (R + T);
      Vector3D f = new Vector3D(T * pdfVndfGGX(wm, ax, ay) * G_GGX(wo, refraction.wt(), ax, ay)
          * Math.abs(refraction.wt().dot(wm) * wo.dot(wm) / (cosTheta(refraction.wt()) * cosTheta(wo) * denom))).div(sqr(refraction.eta()))
          .mult(MathUtils.max(sqrtBaseColor, 1e-4D));
      return new BxDFSample(shadingFrame.toWorld(refraction.wt()), f, pdf, BxDF.Event.TRANSMIT, true);
    }
  }
}
