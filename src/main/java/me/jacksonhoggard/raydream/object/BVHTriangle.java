package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector3D;

import java.util.ArrayList;
import java.util.List;

public class BVHTriangle {

    private final Node root;

    public BVHTriangle(Triangle[] triangles) {
        this.root = new Node();
        root.firstObject = 0;
        root.objectCount = triangles.length;
        root.left = null;
        root.right = null;
        updateNodeBounds(root, triangles);
        subdivide(root, triangles);
    }

    public double intersect(Ray ray, Triangle[] triangles, Triangle triangleHit) {
        List<Node> stack = new ArrayList<>();
        Node currentNode = root;
        double t = Double.MAX_VALUE;
        while(true) {
            if(intersectAABB(ray, currentNode.min, currentNode.max)) {
                if(!currentNode.isLeaf()) {
                    stack.add(currentNode.right);
                    currentNode = currentNode.left;
                    continue;
                } else {
                    for(int i = currentNode.firstObject; i < currentNode.firstObject + currentNode.objectCount; i++) {
                        double temp = triangles[i].intersect(ray);
                        if(temp > 0 && temp < t) {
                            t = temp;
                            triangleHit.set(triangles[i]);
                        }
                    }
                }
            }
            if(stack.isEmpty())
                break;
            currentNode = stack.removeLast();
        }
        return t;
    }

    private boolean intersectAABB(Ray ray, Vector3D min, Vector3D max) {
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
            return false;

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

        return (!(tMin > tZMax)) && (!(tZMin > tMax));
    }

    private void updateNodeBounds(Node node, Triangle[] triangles) {
        node.min = new Vector3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        node.max = new Vector3D(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
        for(int first = node.firstObject, i = 0; i < node.objectCount; i++) {
            node.min.x = Math.min(triangles[first + i].getMin().x, node.min.x);
            node.min.y = Math.min(triangles[first + i].getMin().y, node.min.y);
            node.min.z = Math.min(triangles[first + i].getMin().z, node.min.z);
            node.max.x = Math.max(triangles[first + i].getMax().x, node.max.x);
            node.max.y = Math.max(triangles[first + i].getMax().y, node.max.y);
            node.max.z = Math.max(triangles[first + i].getMax().z, node.max.z);
        }
    }

    private void subdivide(Node node, Triangle[] triangles) {
        Vector3D extent = Vector3D.sub(node.max, node.min);
        double splitPos = node.min.z + extent.z * 0.5D;
        int axis = 2;
        if(extent.x > extent.y && extent.x > extent.z) {
            splitPos = node.min.x + extent.x * 0.5D;
            axis = 0;
        }
        if(extent.y > extent.x && extent.y > extent.z) {
            splitPos = node.min.y + extent.y * 0.5D;
            axis = 1;
        }
        int i = node.firstObject;
        int j = i + node.objectCount - 1;
        while(i <= j) {
            double centroidAxis = switch (axis) {
                case 0 -> triangles[i].getCentroid().x;
                case 1 -> triangles[i].getCentroid().y;
                case 2 -> triangles[i].getCentroid().z;
                default -> 0;
            };
            if(centroidAxis < splitPos)
                i++;
            else {
                Triangle temp = triangles[i];
                triangles[i] = triangles[j];
                triangles[j] = temp;
                j--;
            }
        }

        int leftCount = i - node.firstObject;
        if(leftCount == 0 || leftCount == node.objectCount)
            return;
        node.left = new Node();
        node.left.firstObject = node.firstObject;
        node.left.objectCount = leftCount;
        node.right = new Node();
        node.right.firstObject = i;
        node.right.objectCount = node.objectCount - leftCount;
        node.objectCount = 0;
        updateNodeBounds(node.left, triangles);
        updateNodeBounds(node.right, triangles);
        subdivide(node.left, triangles);
        subdivide(node.right, triangles);
    }

    private class Node {
        private Node left;
        private Node right;
        private int firstObject;
        private int objectCount;
        private Vector3D min;
        private Vector3D max;

        private boolean isLeaf() {
            return left == null && right == null;
        }
    }

}