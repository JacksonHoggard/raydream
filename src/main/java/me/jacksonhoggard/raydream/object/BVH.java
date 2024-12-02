package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.math.Vector4D;

import java.util.ArrayList;
import java.util.List;

public class BVH {

    private final Node root;

    public BVH(Object[] objects) {
        this.root = new Node();
        root.firstObject = 0;
        root.objectCount = objects.length;
        root.left = null;
        root.right = null;
        updateNodeBounds(root, objects);
        subdivide(root, objects);
    }

    public Hit intersect(Ray ray, Object[] objects) {
        List<Node> stack = new ArrayList<>();
        Node currentNode = root;
        Hit out = new Hit(null, null, null, null, null, Double.MAX_VALUE);
        while(true) {
            if(currentNode.isLeaf()) {
                for(int i = currentNode.firstObject; i < currentNode.firstObject + currentNode.objectCount; i++) {
                    Vector4D rOriginOS = new Vector4D(ray.origin().x, ray.origin().y, ray.origin().z, 1);
                    Vector4D rDirOS = new Vector4D(ray.direction().x, ray.direction().y, ray.direction().z, 0);
                    rOriginOS = rOriginOS.mult(objects[i].getInverseTransformMatrix());
                    rDirOS = rDirOS.mult(objects[i].getInverseTransformMatrix());
                    Ray rayOS = new Ray(new Vector3D(rOriginOS.x, rOriginOS.y, rOriginOS.z), new Vector3D(rDirOS.x, rDirOS.y, rDirOS.z));
                    Hit hit = objects[i].intersect(rayOS);
                    if (hit.object() != null && hit.t() > 0 && hit.t() < out.t()) {
                        if(hit.object() instanceof Model)
                            out = new Hit(hit.object(), hit.triangle(), ray.at(hit.t()), hit.normal(), hit.texCoord(), hit.t());
                        else
                            out = new Hit(hit.object(), null, ray.at(hit.t()), hit.normal(), hit.texCoord(), hit.t());
                    }
                }
                if(stack.isEmpty())
                    break;
                else currentNode = stack.removeLast();
                continue;
            }
            Node left = currentNode.left;
            Node right = currentNode.right;
            double distL = intersectAABB(ray, left.min, left.max, out.t());
            double distR = intersectAABB(ray, right.min, right.max, out.t());
            if(distL > distR) {
                double temp = distL;
                distL = distR;
                distR = temp;
                Node tempNode = new Node();
                tempNode.set(left);
                left = right;
                right = tempNode;
            }
            if(distL == Double.MAX_VALUE) {
                if (stack.isEmpty())
                    break;
                currentNode = stack.removeLast();
            } else {
                currentNode = left;
                if(distR != Double.MAX_VALUE) stack.add(right);
            }
        }
        return out;
    }

    private double intersectAABB(Ray ray, Vector3D min, Vector3D max, double t) {
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
            return Double.MAX_VALUE;

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
            return Double.MAX_VALUE;

        if(tZMin > tMin)
            tMin = tZMin;
        if(tZMax < tMax)
            tMax = tZMax;

        if(tMin < 0 && tMax >= 0)
            return tMax;

        if(tMin >= 0 && tMin < t)
            return tMin;

        return Double.MAX_VALUE;
    }

    public boolean intersectShadowRay(Ray ray, Object[] objects, double lightDistance) {
        List<Node> stack = new ArrayList<>();
        Node currentNode = root;
        double t = lightDistance;
        while(true) {
            if(currentNode.isLeaf()) {
                for(int i = currentNode.firstObject; i < currentNode.firstObject + currentNode.objectCount; i++) {
                    Vector4D rOriginOS = new Vector4D(ray.origin().x, ray.origin().y, ray.origin().z, 1);
                    Vector4D rDirOS = new Vector4D(ray.direction().x, ray.direction().y, ray.direction().z, 0);
                    rOriginOS = rOriginOS.mult(objects[i].getInverseTransformMatrix());
                    rDirOS = rDirOS.mult(objects[i].getInverseTransformMatrix());
                    Ray rayOS = new Ray(new Vector3D(rOriginOS.x, rOriginOS.y, rOriginOS.z), new Vector3D(rDirOS.x, rDirOS.y, rDirOS.z));
                    if(objects[i] instanceof Model) {
                        if(((Model) objects[i]).intersectShadowRay(rayOS, t)) {
                            return true;
                        }
                        continue;
                    }
                    Hit hit = objects[i].intersect(rayOS);
                    if (hit.object() != null && hit.t() > 0 && hit.t() < t) {
                        return true; // Early out
                    }
                }
                if(stack.isEmpty())
                    break;
                else currentNode = stack.removeLast();
                continue;
            }
            Node left = currentNode.left;
            Node right = currentNode.right;
            double distL = intersectAABB(ray, left.min, left.max, t);
            double distR = intersectAABB(ray, right.min, right.max, t);
            if(distL > distR) {
                double temp = distL;
                distL = distR;
                distR = temp;
                Node tempNode = new Node();
                tempNode.set(left);
                left = right;
                right = tempNode;
            }
            if(distL == Double.MAX_VALUE) {
                if (stack.isEmpty())
                    break;
                currentNode = stack.removeLast();
            } else {
                currentNode = left;
                if(distR != Double.MAX_VALUE) stack.add(right);
            }
        }
        return false;
    }

    private void updateNodeBounds(Node node, Object[] objects) {
        node.min = new Vector3D(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        node.max = new Vector3D(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
        for(int first = node.firstObject, i = 0; i < node.objectCount; i++) {
            node.min.x = Math.min(objects[first + i].getMin().x, node.min.x);
            node.min.y = Math.min(objects[first + i].getMin().y, node.min.y);
            node.min.z = Math.min(objects[first + i].getMin().z, node.min.z);
            node.max.x = Math.max(objects[first + i].getMax().x, node.max.x);
            node.max.y = Math.max(objects[first + i].getMax().y, node.max.y);
            node.max.z = Math.max(objects[first + i].getMax().z, node.max.z);
        }
    }

    private void subdivide(Node node, Object[] objects) {
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
                case 0 -> objects[i].getCentroid().x;
                case 1 -> objects[i].getCentroid().y;
                case 2 -> objects[i].getCentroid().z;
                default -> 0;
            };
            if(centroidAxis < splitPos)
                i++;
            else {
                Object temp = objects[i];
                objects[i] = objects[j];
                objects[j] = temp;
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
        updateNodeBounds(node.left, objects);
        updateNodeBounds(node.right, objects);
        subdivide(node.left, objects);
        subdivide(node.right, objects);
    }

    private class Node {
        private Node left;
        private Node right;
        private int firstObject;
        private int objectCount;
        private Vector3D min;
        private Vector3D max;

        public void set(Node node) {
            this.left = node.left;
            this.right = node.right;
            this.firstObject = node.firstObject;
            this.objectCount = node.objectCount;
            this.min = node.min;
            this.max = node.max;
        }

        private boolean isLeaf() {
            return left == null && right == null;
        }
    }

}
