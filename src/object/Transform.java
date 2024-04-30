package object;

import math.Vector3D;

public record Transform(Vector3D translation, Vector3D rotation, Vector3D scale){

    public Vector3D rotatePoint(Vector3D point) {
        double cosa = Math.cos(rotation.z);
        double sina = Math.sin(rotation.z);

        double cosb = Math.cos(rotation.x);
        double sinb = Math.sin(rotation.x);

        double cosc = Math.cos(rotation.y);
        double sinc = Math.sin(rotation.y);

        double Axx = cosa*cosb;
        double Axy = cosa*sinb*sinc - sina*cosc;
        double Axz = cosa*sinb*cosc + sina*sinc;

        double Ayx = sina*cosb;
        double Ayy = sina*sinb*sinc + cosa*cosc;
        double Ayz = sina*sinb*cosc - cosa*sinc;

        double Azx = -sinb;
        double Azy = cosb*sinc;
        double Azz = cosb*cosc;

        return new Vector3D(
                Axx * point.x + Axy * point.y + Axz * point.z,
                Ayx * point.x + Ayy * point.y + Ayz * point.z,
                Azx * point.x + Azy * point.y + Azz * point.z
        );
    }

}
