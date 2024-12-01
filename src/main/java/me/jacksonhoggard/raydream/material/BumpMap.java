package me.jacksonhoggard.raydream.material;

import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

import java.awt.image.BufferedImage;

public class BumpMap extends Texture {

    private final double scale;
    private final double deltaX, deltaY;

    public BumpMap(BufferedImage image, String path, int width, int height, double scale) {
        super(image, path, width, height);
        this.scale = scale;
        this.deltaX = 1.d / width;
        this.deltaY = 1.d / height;
    }

    public Vector3D apply(Vector3D normal, Vector3D tangent, Vector3D bitangent, Vector2D texCoords) {
        int x = (int) Math.round(texCoords.x * (getWidth() - 1));
        int y = (int) Math.round(texCoords.y * (getHeight() - 1));
        int x1 = (int) Math.round((texCoords.x + deltaX) * (getWidth() - 1));
        int y1 = (int) Math.round((texCoords.y + deltaY) * (getHeight() - 1));
        double height = getHeightAt(x, y);
        double dU = getHeightAt(x1 % (getWidth() - 1), y) - height;
        double dV = getHeightAt(x, y1 % (getHeight() - 1)) - height;
        dU *= scale;
        dV *= scale;
        Vector3D qu = Vector3D.add(tangent, Vector3D.mult(normal, -dU));
        Vector3D qv = Vector3D.add(bitangent, Vector3D.mult(normal, -dV));
        return qv.cross(qu).normalize();

//        Vector3D tangentTS = new Vector3D(1, 0, dU);
//        Vector3D bitangentTS = new Vector3D(0, 1, dV);
//        Vector3D perturbedTS = tangentTS.cross(bitangentTS).normalize();
//
//
//        Vector3D perturbed = new Vector3D(
//                tangent.x*perturbedTS.x + bitangent.x*perturbedTS.y + normal.x*perturbedTS.z,
//                tangent.y*perturbedTS.x + bitangent.y*perturbedTS.y + normal.y*perturbedTS.z,
//                tangent.z*perturbedTS.x + bitangent.z*perturbedTS.y + normal.z*perturbedTS.z
//        );
//        Vector3D perturbed = new Vector3D(-dU, -dV, 1).normalize();
        // Vector3D perturbed = new Vector3D(normal);
//        perturbed.add(Vector3D.mult(tangent, dU));
//        perturbed.add(Vector3D.mult(bitangent, dV));
        // perturbed.set(tangent.mult(perturbed.x).add(bitangent.mult(perturbed.y)).add(normal.mult(perturbed.z)));
        //return perturbed.normalize();
    }

    private double getHeightAt(int x, int y) {
        int color = getImage().getRGB(x, y);
        return ((((color & 0xff0000) >> 16) / 255D)
                + (((color & 0xff00) >> 8) / 255D)
                + (((color & 0xff)) / 255D)) / 3.0D;
    }
}
