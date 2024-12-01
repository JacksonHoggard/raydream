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
        double tMin, tMax, tYMin, tYMax, tZMin, tZMax;
        if(ray.direction().x >= 0) {
            tMin = (min.x - ray.origin().x) / ray.direction().x;
            tMax = (max.x - ray.origin().x) / ray.direction().x;
        } else {
            tMin = (max.x - ray.origin().x) / ray.direction().x;
            tMax = (min.x - ray.origin().x) / ray.direction().x;
        }
        if(ray.direction().y >= 0) {
            tYMin = (min.y - ray.origin().y) / ray.direction().y;
            tYMax = (max.y - ray.origin().y) / ray.direction().y;
        } else {
            tYMin = (max.y - ray.origin().y) / ray.direction().y;
            tYMax = (min.y - ray.origin().y) / ray.direction().y;
        }
        if((tMin > tYMax) || (tYMin > tMax))
            return new Hit(null, null, null, null, null, -1.0D);

        if (tYMin > tMin)
            tMin = tYMin;
        if (tYMax < tMax)
            tMax = tYMax;

        if(ray.direction().z >= 0) {
            tZMin = (min.z - ray.origin().z) / ray.direction().z;
            tZMax = (max.z - ray.origin().z) / ray.direction().z;
        } else {
            tZMin = (max.z - ray.origin().z) / ray.direction().z;
            tZMax = (min.z - ray.origin().z) / ray.direction().z;
        }

        if((tMin > tZMax) || (tZMin > tMax))
            return new Hit(null, null, null, null, null, -1.0D);

        if(tZMin > tMin)
            tMin = tZMin;
        if(tZMax < tMax)
            tMax = tZMax;

        if(tMin < 0 && tMax >= 0)
            return new Hit(this, null, ray.at(tMax), normalAt(ray.at(tMax)), mapTexture(ray.at(tMax)), tMax);

        if(tMin >= 0)
            return new Hit(this, null, ray.at(tMin), normalAt(ray.at(tMin)), mapTexture(ray.at(tMin)), tMin);

        return new Hit(null, null, null, null, null, -1.0D);
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
    public Vector3D calcTangent(Vector3D normal) {
//        if(normal.equals(new Vector3D(1, 0, 0))) {
//            return new Vector3D(0, 0, -1);
//        }
//        if(normal.equals(new Vector3D(-1, 0, 0))) {
//            return new Vector3D(0, 0, 1);
//        }
//        if(normal.equals(new Vector3D(0, 1, 0))) {
//            return new Vector3D(1, 0, 0);
//        }
//        if(normal.equals(new Vector3D(0, -1, 0))) {
//            return new Vector3D(1, 0, 0);
//        }
//        if(normal.equals(new Vector3D(0, 0, 1))) {
//            return new Vector3D(1, 0, 0);
//        }
//        return new Vector3D(-1, 0, 0);
        Vector3D arbitraryVector = Math.abs(normal.x) < 0.5d ? new Vector3D(1, 0, 0) : new Vector3D(0, 1, 0);
        return arbitraryVector.cross(normal).normalize();
    }

    @Override
    public Vector3D calcBitangent(Vector3D normal, Vector3D tangent) {
        Vector3D bitangent = normal.cross(tangent).normalize();
        if(tangent.cross(bitangent).dot(normal) > 0.0d)
            tangent.negate();
        return bitangent;
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
        return new Vector2D(
                (point.x + 0.5) % 1.0D,
                (point.y + 0.5) % 1.0D
        );
    }

    private Vector2D uvBack(Vector3D point) {
        return new Vector2D(
                1 - ((point.x + 0.5) % 1.0D),
                (point.y + 0.5) % 1.0D
        );
    }

    private Vector2D uvLeft(Vector3D point) {
        return new Vector2D(
                (point.z + 0.5) % 1.0D,
                (point.y + 0.5) % 1.0D
        );
    }

    private Vector2D uvRight(Vector3D point) {
        return new Vector2D(
                1 - ((point.z + 0.5) % 1.0D),
                (point.y + 0.5) % 1.0D
        );
    }

    private Vector2D uvUp(Vector3D point) {
        return new Vector2D(
                (point.x + 0.5) % 1.0D,
                1 - ((point.z + 0.5) % 1.0D)
        );
    }

    private Vector2D uvDown(Vector3D point) {
        return new Vector2D(
                (point.x + 0.5) % 1.0D,
                (point.z + 0.5) % 1.0D
        );
    }

    public Vector3D getSize() {
        return size;
    }
}
