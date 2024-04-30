package object;

import math.Vector3D;

public record Hit(Object object, Vector3D point, Vector3D normal, double t) {

}
