import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;
import me.jacksonhoggard.raydream.object.Triangle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ThreadLocalRandom;

public class TestTriangle {
    @Test
    public void testBarycentric() {
        Vector3D a = new Vector3D(1.0, 2.0, 3.0);
        Vector3D b = new Vector3D(-4.0, -5.0, -6.0);
        Vector3D c = new Vector3D(7.0, 8.0, 9.0);
        Triangle t = new Triangle(a, b, c, new Vector2D(), new Vector2D(), new Vector2D());
        double randomA = ThreadLocalRandom.current().nextDouble(0, 1);
        double randomB = ThreadLocalRandom.current().nextDouble(0, 1);
        Vector3D p = Vector3D.mult(a, 1-Math.sqrt(randomA)).add(Vector3D.mult(Math.sqrt(randomA) * (1-randomB), b).add(Vector3D.mult(randomB*Math.sqrt(randomA), c)));
        Vector3D v = new Vector3D();
        t.calcBarycentric(p, v);
        Assertions.assertTrue(v.y >= 0.0d && v.z >= 0.0d && (v.y + v.z) <= 1.0d);
    }
}
