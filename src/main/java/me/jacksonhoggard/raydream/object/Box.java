package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.material.Material;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;

public class Box extends Object {

    private final Vector3D center;
    private final Vector3D min;
    private final Vector3D max;
    private final Vector3D size;

    public Box(Transform transform, Vector3D size, Material material) {
        super(transform, material, new Vector3D(-size.x/2, -size.y/2, -size.z/2), new Vector3D(size.x/2, size.y/2, size.z/2));
        this.min = new Vector3D(-size.x/2, -size.y/2, -size.z/2);
        this.max = new Vector3D(size.x/2, size.y/2, size.z/2);
        this.size = size;
        this.center = Vector3D.add(min, max).div(2.0D);
    }

    @Override
    public Hit intersect(Ray ray) {
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
            return new Hit(null, null, null, -1.0D);

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
            return new Hit(null, null, null, -1.0D);

        if(tZMin > tMin)
            tMin = tZMin;

        return new Hit(this, ray.at(tMin), transformNormalToWS(normalAt(ray.at(tMin)), getNormalMatrix()), tMin);
    }

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
        Vector3D normal = normalAt(point);
        if(normal.equals(new Vector3D(1, 0, 0))) return uvRight(point);
        if(normal.equals(new Vector3D(-1, 0, 0))) return uvLeft(point);
        if(normal.equals(new Vector3D(0, 1, 0))) return uvUp(point);
        if(normal.equals(new Vector3D(0, -1, 0))) return uvDown(point);
        if(normal.equals(new Vector3D(0, 0, 1))) return uvFront(point);
        return uvBack(point);
    }

    private Vector2D uvFront(Vector3D point) {
        double lengthU = Math.abs(max.x - min.x);
        double lengthV = Math.abs(max.y - min.y);
        return new Vector2D(
                (point.x % lengthU) / lengthU,
                (point.y % lengthV) / lengthV
        );
    }

    private Vector2D uvBack(Vector3D point) {
        double lengthU = Math.abs(max.x - min.x);
        double lengthV = Math.abs(max.y - min.y);
        return new Vector2D(
                1 - ((point.x % lengthU) / lengthU),
                (point.y % lengthV) / lengthV
        );
    }

    private Vector2D uvLeft(Vector3D point) {
        double lengthU = Math.abs(max.z - min.z);
        double lengthV = Math.abs(max.y - min.y);
        return new Vector2D(
                (point.z % lengthU) / lengthU,
                (point.y % lengthV) / lengthV
        );
    }

    private Vector2D uvRight(Vector3D point) {
        double lengthU = Math.abs(max.z - min.z);
        double lengthV = Math.abs(max.y - min.y);
        return new Vector2D(
                1 - ((point.z % lengthU) / lengthU),
                (point.y % lengthV) / lengthV
        );
    }

    private Vector2D uvUp(Vector3D point) {
        double lengthU = Math.abs(max.x - min.x);
        double lengthV = Math.abs(max.z - min.z);
        return new Vector2D(
                ((point.x % lengthU) / lengthU),
                1 - ((point.z % lengthV) / lengthV)
        );
    }

    private Vector2D uvDown(Vector3D point) {
        double lengthU = Math.abs(max.x - min.x);
        double lengthV = Math.abs(max.z - min.z);
        return new Vector2D(
                (point.x % lengthU) / lengthU,
                (point.z % lengthV) / lengthV
        );
    }

    public Vector3D getSize() {
        return size;
    }
}
