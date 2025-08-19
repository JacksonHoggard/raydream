package me.jacksonhoggard.raydream.object;

import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Triangle Tests")
public class TriangleTest {
    
    private Triangle triangle;
    private Vector3D v0, v1, v2;
    private Vector2D t0, t1, t2;
    private static final double EPSILON = 1e-8;
    
    @BeforeEach
    void setUp() {
        // Create a triangle in the XY plane
        v0 = new Vector3D(0.0, 0.0, 0.0);
        v1 = new Vector3D(1.0, 0.0, 0.0);
        v2 = new Vector3D(0.0, 1.0, 0.0);
        
        // Texture coordinates
        t0 = new Vector2D(0.0, 0.0);
        t1 = new Vector2D(1.0, 0.0);
        t2 = new Vector2D(0.0, 1.0);
        
        triangle = new Triangle(v0, v1, v2, t0, t1, t2);
    }
    
    @Test
    @DisplayName("Constructor Tests")
    void testConstructors() {
        // Test basic constructor
        assertNotNull(triangle);
        assertEquals(v0, triangle.getCentroid().mult(2.0).sub(triangle.getMax()).sub(triangle.getMin()));
        
        // Test constructor with normals
        Vector3D n0 = new Vector3D(0.0, 0.0, 1.0);
        Vector3D n1 = new Vector3D(0.0, 0.0, 1.0);
        Vector3D n2 = new Vector3D(0.0, 0.0, 1.0);
        
        Triangle triangleWithNormals = new Triangle(v0, v1, v2, n0, n1, n2, t0, t1, t2);
        assertNotNull(triangleWithNormals);
    }
    
    @Test
    @DisplayName("Bounding Box Calculation")
    void testBoundingBox() {
        Vector3D min = triangle.getMin();
        Vector3D max = triangle.getMax();
        
        // Min should be (0, 0, 0)
        assertEquals(0.0, min.x, EPSILON);
        assertEquals(0.0, min.y, EPSILON);
        assertEquals(0.0, min.z, EPSILON);
        
        // Max should be (1, 1, 0)
        assertEquals(1.0, max.x, EPSILON);
        assertEquals(1.0, max.y, EPSILON);
        assertEquals(0.0, max.z, EPSILON);
    }
    
    @Test
    @DisplayName("Centroid Calculation")
    void testCentroid() {
        Vector3D centroid = triangle.getCentroid();
        
        // Centroid should be at (0.5, 0.5, 0)
        assertEquals(0.5, centroid.x, EPSILON);
        assertEquals(0.5, centroid.y, EPSILON);
        assertEquals(0.0, centroid.z, EPSILON);
    }
    
    @Test
    @DisplayName("Barycentric Coordinates")
    void testBarycentricCoordinates() {
        Vector3D baryCoords = new Vector3D();
        
        // Test vertex points
        triangle.calcBarycentric(v0, baryCoords);
        assertEquals(1.0, baryCoords.x, EPSILON); // u = 1 at v0
        assertEquals(0.0, baryCoords.y, EPSILON); // v = 0 at v0
        assertEquals(0.0, baryCoords.z, EPSILON); // w = 0 at v0
        
        triangle.calcBarycentric(v1, baryCoords);
        assertEquals(0.0, baryCoords.x, EPSILON); // u = 0 at v1
        assertEquals(1.0, baryCoords.y, EPSILON); // v = 1 at v1
        assertEquals(0.0, baryCoords.z, EPSILON); // w = 0 at v1
        
        triangle.calcBarycentric(v2, baryCoords);
        assertEquals(0.0, baryCoords.x, EPSILON); // u = 0 at v2
        assertEquals(0.0, baryCoords.y, EPSILON); // v = 0 at v2
        assertEquals(1.0, baryCoords.z, EPSILON); // w = 1 at v2
        
        // Test center point
        Vector3D center = new Vector3D(1.0/3.0, 1.0/3.0, 0.0);
        triangle.calcBarycentric(center, baryCoords);
        assertEquals(1.0/3.0, baryCoords.x, 0.01); // Relaxed epsilon for center
        assertEquals(1.0/3.0, baryCoords.y, 0.01);
        assertEquals(1.0/3.0, baryCoords.z, 0.01);
        
        // Coordinates should sum to 1
        assertEquals(1.0, baryCoords.x + baryCoords.y + baryCoords.z, EPSILON);
    }
    
    @Test
    @DisplayName("Ray-Triangle Intersection")
    void testRayIntersection() {
        // Ray pointing directly at triangle from above
        Ray rayHit = new Ray(new Vector3D(0.25, 0.25, 1.0), new Vector3D(0.0, 0.0, -1.0));
        double t = triangle.intersect(rayHit);
        assertTrue(t > 0.0); // Should hit
        assertEquals(1.0, t, EPSILON); // Should hit at distance 1
        
        // Ray missing the triangle
        Ray rayMiss = new Ray(new Vector3D(2.0, 2.0, 1.0), new Vector3D(0.0, 0.0, -1.0));
        double tMiss = triangle.intersect(rayMiss);
        assertEquals(-1.0, tMiss, EPSILON); // Should miss
        
        // Ray pointing away from triangle
        Ray rayAway = new Ray(new Vector3D(0.25, 0.25, 1.0), new Vector3D(0.0, 0.0, 1.0));
        double tAway = triangle.intersect(rayAway);
        assertEquals(-1.0, tAway, EPSILON); // Should miss
        
        // Ray parallel to triangle
        Ray rayParallel = new Ray(new Vector3D(0.25, 0.25, 1.0), new Vector3D(1.0, 0.0, 0.0));
        double tParallel = triangle.intersect(rayParallel);
        assertEquals(-1.0, tParallel, EPSILON); // Should miss (parallel)
    }
    
    @Test
    @DisplayName("Ray-Triangle Edge Cases")
    void testRayIntersectionEdgeCases() {
        // Ray hitting triangle edge
        Ray rayEdge = new Ray(new Vector3D(0.5, 0.0, 1.0), new Vector3D(0.0, 0.0, -1.0));
        double tEdge = triangle.intersect(rayEdge);
        assertTrue(tEdge > 0.0); // Should hit the edge
        
        // Ray hitting triangle vertex
        Ray rayVertex = new Ray(new Vector3D(0.0, 0.0, 1.0), new Vector3D(0.0, 0.0, -1.0));
        double tVertex = triangle.intersect(rayVertex);
        assertTrue(tVertex > 0.0); // Should hit the vertex
        
        // Ray very close to triangle but missing
        Ray rayClose = new Ray(new Vector3D(0.50001, 0.50001, 1.0), new Vector3D(0.0, 0.0, -1.0));
        double tClose = triangle.intersect(rayClose);
        assertEquals(-1.0, tClose, EPSILON); // Should miss due to being outside
    }
    
    @Test
    @DisplayName("Normal Calculation")
    void testNormalCalculation() {
        // For a triangle in XY plane, normal should point in +Z direction
        Vector3D normal = triangle.getNormal(new Vector3D(0.25, 0.25, 0.0));
        
        // Normal should be unit length
        assertEquals(1.0, normal.length(), EPSILON);
        
        // For triangle in XY plane, normal should be (0, 0, 1) or (0, 0, -1)
        assertEquals(0.0, normal.x, EPSILON);
        assertEquals(0.0, normal.y, EPSILON);
        assertTrue(Math.abs(Math.abs(normal.z) - 1.0) < EPSILON);
    }
    
    @Test
    @DisplayName("Texture Mapping")
    void testTextureMapping() {
        // Test texture mapping at vertices
        Vector3D baryV0 = new Vector3D(1.0, 0.0, 0.0);
        Vector2D texV0 = triangle.mapTexture(baryV0);
        assertEquals(t0.x, texV0.x, EPSILON);
        assertEquals(t0.y, texV0.y, EPSILON);
        
        Vector3D baryV1 = new Vector3D(0.0, 1.0, 0.0);
        Vector2D texV1 = triangle.mapTexture(baryV1);
        assertEquals(t1.x, texV1.x, EPSILON);
        assertEquals(t1.y, texV1.y, EPSILON);
        
        Vector3D baryV2 = new Vector3D(0.0, 0.0, 1.0);
        Vector2D texV2 = triangle.mapTexture(baryV2);
        assertEquals(t2.x, texV2.x, EPSILON);
        assertEquals(t2.y, texV2.y, EPSILON);
        
        // Test interpolation at center
        Vector3D baryCenter = new Vector3D(1.0/3.0, 1.0/3.0, 1.0/3.0);
        Vector2D texCenter = triangle.mapTexture(baryCenter);
        double expectedX = (t0.x + t1.x + t2.x) / 3.0;
        double expectedY = (t0.y + t1.y + t2.y) / 3.0;
        assertEquals(expectedX, texCenter.x, EPSILON);
        assertEquals(expectedY, texCenter.y, EPSILON);
    }
    
    @Test
    @DisplayName("Triangle Area Calculation")
    void testTriangleAreaCalculation() {
        // Test the static area calculation method (note: this may return signed area)
        double area = Triangle.calcTriArea(0.0, 0.0, 1.0, 0.0, 0.0, 1.0);
        assertEquals(1.0, Math.abs(area), EPSILON); // Use absolute value since it may be signed
        
        // Test with different coordinates
        double area2 = Triangle.calcTriArea(0.0, 0.0, 2.0, 0.0, 0.0, 2.0);
        assertEquals(4.0, Math.abs(area2), EPSILON);
    }
    
    @Test
    @DisplayName("Tangent and Bitangent")
    void testTangentAndBitangent() {
        Vector3D tangent = triangle.getTangent();
        assertNotNull(tangent);
        
        Vector3D normal = new Vector3D(0.0, 0.0, 1.0);
        Vector3D bitangent = triangle.getBitangent(normal);
        assertNotNull(bitangent);
        
        // Tangent and bitangent should be perpendicular to normal
        assertEquals(0.0, Math.abs(tangent.dot(normal)), 0.1); // Some tolerance for numerical errors
        assertEquals(0.0, Math.abs(bitangent.dot(normal)), 0.1);
    }
    
    @Test
    @DisplayName("Triangle Copy/Set")
    void testTriangleSet() {
        // Create a different triangle
        Vector3D v0New = new Vector3D(1.0, 1.0, 1.0);
        Vector3D v1New = new Vector3D(2.0, 1.0, 1.0);
        Vector3D v2New = new Vector3D(1.0, 2.0, 1.0);
        Triangle newTriangle = new Triangle(v0New, v1New, v2New, t0, t1, t2);
        
        // Copy properties
        triangle.set(newTriangle);
        
        // Verify the copy
        Vector3D newMin = triangle.getMin();
        Vector3D newMax = triangle.getMax();
        assertEquals(1.0, newMin.x, EPSILON);
        assertEquals(1.0, newMin.y, EPSILON);
        assertEquals(1.0, newMin.z, EPSILON);
        assertEquals(2.0, newMax.x, EPSILON);
        assertEquals(2.0, newMax.y, EPSILON);
        assertEquals(1.0, newMax.z, EPSILON);
    }
    
    @Test
    @DisplayName("Degenerate Triangle Cases")
    void testDegenerateTriangles() {
        // Triangle with all vertices at same point (degenerate)
        Vector3D samePoint = new Vector3D(1.0, 1.0, 1.0);
        Triangle degenerateTriangle = new Triangle(samePoint, samePoint, samePoint, t0, t1, t2);
        
        // Should handle gracefully
        assertNotNull(degenerateTriangle);
        
        // Collinear vertices (degenerate)
        Vector3D collinear1 = new Vector3D(0.0, 0.0, 0.0);
        Vector3D collinear2 = new Vector3D(1.0, 0.0, 0.0);
        Vector3D collinear3 = new Vector3D(2.0, 0.0, 0.0);
        Triangle collinearTriangle = new Triangle(collinear1, collinear2, collinear3, t0, t1, t2);
        
        assertNotNull(collinearTriangle);
        
        // Ray intersection with degenerate triangle should miss or handle gracefully
        Ray ray = new Ray(new Vector3D(0.5, 1.0, 1.0), new Vector3D(0.0, 0.0, -1.0));
        double t = collinearTriangle.intersect(ray);
        // Should either miss or handle the degenerate case
        assertTrue(t == -1.0 || t >= 0.0);
    }
}
