package object;

public class Mesh {

    private final Triangle[] triangles;

    public Mesh(Triangle[] triangles) {
        this.triangles = triangles;
    }

    public Triangle[] getTriangles() {
        return triangles;
    }
}
