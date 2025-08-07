package me.jacksonhoggard.raydream.acceleration;

import me.jacksonhoggard.raydream.config.ApplicationConfig;
import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Hit;
import me.jacksonhoggard.raydream.object.Object;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Improved Bounding Volume Hierarchy implementation for efficient ray-object intersection.
 * This replaces the original BVH with better splitting heuristics and memory layout.
 */
public class ImprovedBVH {

    private BVHNode root;
    private final List<Object> objects;

    public ImprovedBVH(List<Object> objects) {
        this.objects = new ArrayList<>(objects);
        this.root = buildBVH(this.objects, 0);
    }

    /**
     * Finds the closest intersection along a ray.
     * @param ray the ray to intersect
     * @param tMin minimum distance
     * @param tMax maximum distance
     * @return intersection result or null if no intersection
     */
    public Hit intersect(Ray ray, double tMin, double tMax) {
        if (root == null) return null;
        return intersectNode(root, ray, tMin, tMax);
    }

    private BVHNode buildBVH(List<Object> objects, int depth) {
        if (objects.isEmpty()) return null;

        BVHNode node = new BVHNode();
        node.bounds = calculateBounds(objects);

        // Leaf node condition
        if (objects.size() <= ApplicationConfig.BVH_MAX_OBJECTS_PER_LEAF || depth > 20) {
            node.objects = new ArrayList<>(objects);
            return node;
        }

        // Choose split axis (Surface Area Heuristic)
        int bestAxis = chooseSplitAxis(objects, node.bounds);

        // Sort objects along the chosen axis
        objects.sort(getComparator(bestAxis));

        // Split objects
        int midPoint = objects.size() / 2;
        List<Object> leftObjects = objects.subList(0, midPoint);
        List<Object> rightObjects = objects.subList(midPoint, objects.size());

        // Recursively build children
        node.left = buildBVH(new ArrayList<>(leftObjects), depth + 1);
        node.right = buildBVH(new ArrayList<>(rightObjects), depth + 1);

        return node;
    }

    private BoundingBox calculateBounds(List<Object> objects) {
        if (objects.isEmpty()) return new BoundingBox();

        BoundingBox bounds = new BoundingBox(objects.get(0).getBounds());
        for (int i = 1; i < objects.size(); i++) {
            bounds.expand(objects.get(i).getBounds());
        }
        return bounds;
    }

    private int chooseSplitAxis(List<Object> objects, BoundingBox bounds) {
        Vector3D extent = bounds.getExtent();

        // Choose the axis with the largest extent
        if (extent.x >= extent.y && extent.x >= extent.z) return 0; // X axis
        if (extent.y >= extent.z) return 1; // Y axis
        return 2; // Z axis
    }

    private Comparator<Object> getComparator(int axis) {
        return switch (axis) {
            case 0 -> Comparator.comparingDouble(obj -> obj.getBounds().getCenter().x);
            case 1 -> Comparator.comparingDouble(obj -> obj.getBounds().getCenter().y);
            case 2 -> Comparator.comparingDouble(obj -> obj.getBounds().getCenter().z);
            default -> throw new IllegalArgumentException("Invalid axis: " + axis);
        };
    }

    private Hit intersectNode(BVHNode node, Ray ray, double tMin, double tMax) {
        // Check if ray intersects node bounds
        if (!node.bounds.intersects(ray, tMin, tMax)) {
            return null;
        }

        // Leaf node - test objects
        if (node.objects != null) {
            Hit closest = null;
            double closestT = tMax;

            for (Object object : node.objects) {
                Hit result = object.intersect(ray);
                if (result != null && result.t() > tMin && result.t() < closestT) {
                    closest = result;
                    closestT = result.t();
                }
            }
            return closest;
        }

        // Internal node - test children
        Hit leftResult = null;
        Hit rightResult = null;

        if (node.left != null) {
            leftResult = intersectNode(node.left, ray, tMin, tMax);
        }

        double rightTMax = leftResult != null ? leftResult.t() : tMax;
        if (node.right != null) {
            rightResult = intersectNode(node.right, ray, tMin, rightTMax);
        }

        // Return closest intersection
        if (rightResult != null) return rightResult;
        return leftResult;
    }

    private static class BVHNode {
        BoundingBox bounds;
        BVHNode left, right;
        List<Object> objects; // Only used for leaf nodes
    }

    public static class BoundingBox {
        private Vector3D min, max;

        public BoundingBox() {
            this.min = new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
            this.max = new Vector3D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        }

        public BoundingBox(BoundingBox other) {
            this.min = new Vector3D(other.min);
            this.max = new Vector3D(other.max);
        }

        public BoundingBox(Vector3D min, Vector3D max) {
            this.min = new Vector3D(min);
            this.max = new Vector3D(max);
        }

        public void expand(BoundingBox other) {
            min.x = Math.min(min.x, other.min.x);
            min.y = Math.min(min.y, other.min.y);
            min.z = Math.min(min.z, other.min.z);
            max.x = Math.max(max.x, other.max.x);
            max.y = Math.max(max.y, other.max.y);
            max.z = Math.max(max.z, other.max.z);
        }

        public Vector3D getCenter() {
            return new Vector3D(
                (min.x + max.x) * 0.5,
                (min.y + max.y) * 0.5,
                (min.z + max.z) * 0.5
            );
        }

        public Vector3D getExtent() {
            return new Vector3D(
                max.x - min.x,
                max.y - min.y,
                max.z - min.z
            );
        }

        public boolean intersects(Ray ray, double tMin, double tMax) {
            // Fast ray-box intersection using slab method
            Vector3D invDir = new Vector3D(1.0 / ray.direction().x, 1.0 / ray.direction().y, 1.0 / ray.direction().z);
            Vector3D origin = ray.origin();

            double t1 = (min.x - origin.x) * invDir.x;
            double t2 = (max.x - origin.x) * invDir.x;
            double t3 = (min.y - origin.y) * invDir.y;
            double t4 = (max.y - origin.y) * invDir.y;
            double t5 = (min.z - origin.z) * invDir.z;
            double t6 = (max.z - origin.z) * invDir.z;

            double tNear = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
            double tFar = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

            return tFar >= 0 && tNear <= tFar && tNear <= tMax && tFar >= tMin;
        }
    }
}
