package object;

import material.Material;
import math.Ray;
import math.Vector2D;
import math.Vector3D;

public class Box extends Object {

    private final Vector3D center;
    private final Vector3D min;
    private final Vector3D max;

    public Box(Vector3D min, Vector3D max, Material material) {
        super(Vector3D.add(min, max).div(2.0D), material);
        this.center = getPosition();
        this.min = min;
        this.max = max;
    }

    @Override
    public double intersect(Ray ray) {
        double tMin = (min.x - ray.getOrigin().x) / ray.getDirection().x;
        double tMax = (max.x - ray.getOrigin().x) / ray.getDirection().x;

        if(tMin > tMax) {
            double temp = tMin;
            tMin = tMax;
            tMax = temp;
        }

        double tYMin = (min.y - ray.getOrigin().y) / ray.getDirection().y;
        double tYMax = (max.y - ray.getOrigin().y) / ray.getDirection().y;

        if(tYMin > tYMax) {
            double temp = tYMin;
            tYMin = tYMax;
            tYMax = temp;
        }

        if((tMin > tYMax) || (tYMin > tMax))
            return -1.0D;

        if(tYMin > tMin)
            tMin = tYMin;

        if(tYMax < tMax)
            tMax = tYMax;

        double tZMin = (min.z - ray.getOrigin().z) / ray.getDirection().z;
        double tZMax = (max.z - ray.getOrigin().z) / ray.getDirection().z;

        if(tZMin > tZMax) {
            double temp = tZMin;
            tZMin = tZMax;
            tZMax = temp;
        }

        if((tMin > tZMax) || (tZMin > tMax))
            return -1.0D;

        if(tZMin > tMin)
            tMin = tZMin;

        return tMin;
    }

    @Override
    public Vector3D normalAt(Vector3D point) {
        double bias = 1.000001D;
        Vector3D p = Vector3D.sub(point, center);
        Vector3D d = Vector3D.sub(min, max).div(2.0D);
        return new Vector3D(
                (int) (p.x / Math.abs(d.x) * bias),
                (int) (p.y / Math.abs(d.y) * bias),
                (int) (p.z / Math.abs(d.z) * bias)
        ).normalize();
    }

    @Override
    public Vector2D mapTexture(Vector3D point) {
        return null;
    }
}
