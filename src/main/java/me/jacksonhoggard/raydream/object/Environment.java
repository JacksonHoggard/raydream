package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.material.Texture;
import me.jacksonhoggard.raydream.math.Matrix4D;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.util.MathUtils;

public class Environment {
    private Texture texture;
    private Vector3D skyColor;
    private Transform transform;
    private final Matrix4D transformMatrix;
    private final Matrix4D inverseTransformMatrix;
    private final double intensity;

    public Environment(Transform transform, Texture texture, double intensity) {
        this.texture = texture;
        this.transform = transform;
        this.transformMatrix = Object.composeModelMatrix(transform);
        this.intensity = intensity;
        this.inverseTransformMatrix = transformMatrix.inverse();
    }

    public Environment(Vector3D skyColor, double intensity) {
        this(null, null, intensity);
        this.skyColor = skyColor;
    }

    public Vector3D getRadiance(Vector3D direction) {
        if (skyColor != null) {
            // If we have a sky color, use it
            return Vector3D.mult(skyColor, intensity);
        }

        // Apply rotation
        Vector3D d = MathUtils.transformPointToOS(direction, inverseTransformMatrix);

        double phi = Math.atan2(d.z, d.x);
        double theta = Math.acos(Math.clamp(d.y, -1.0, 1.0));
        double u = phi * (1.0D / (2 * Math.PI)) + 0.5D;
        double v = theta * (1.0D / Math.PI);

        // Bilinear sample the HDRI (linear color)
        Vector3D le = texture.sample(new Vector2D(u, v));

        return le.mult(intensity);
    }

    public double getIntensity() {
        return intensity;
    }

}
