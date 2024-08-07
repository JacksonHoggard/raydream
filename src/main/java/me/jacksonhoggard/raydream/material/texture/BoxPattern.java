package me.jacksonhoggard.raydream.material.texture;

import me.jacksonhoggard.raydream.math.Vector3D;

public class BoxPattern extends Texture {

    private final Vector3D main;
    private final Vector3D ul;
    private final Vector3D ur;
    private final Vector3D bl;
    private final Vector3D br;

    public BoxPattern(Vector3D main, Vector3D ul, Vector3D ur, Vector3D bl, Vector3D br) {
        super(null, null, 1, 1);
        this.main = main;
        this.ul = ul;
        this.ur = ur;
        this.bl = bl;
        this.br = br;
    }

    @Override
    protected Vector3D getColorAt(double u, double v) {
        if(v > 0.8D) {
            if(u < 0.2D) return ul;
            if(u > 0.8D) return ur;
        } else if(v < 0.2D) {
            if(u < 0.2D) return bl;
            if(u > 0.8D) return br;
        }
        return main;
    }

    public Vector3D getMain() {
        return main;
    }

    public Vector3D getBl() {
        return bl;
    }

    public Vector3D getBr() {
        return br;
    }

    public Vector3D getUl() {
        return ul;
    }

    public Vector3D getUr() {
        return ur;
    }
}
