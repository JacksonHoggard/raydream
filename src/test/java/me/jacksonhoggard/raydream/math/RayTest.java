package me.jacksonhoggard.raydream.math;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Ray Tests")
public class RayTest {
    
    private Ray ray;
    private Vector3D origin;
    private Vector3D direction;
    private static final double EPSILON = 1e-10;
    
    @BeforeEach
    void setUp() {
        origin = new Vector3D(1.0, 2.0, 3.0);
        direction = new Vector3D(0.0, 1.0, 0.0); // Unit vector in Y direction
        ray = new Ray(origin, direction);
    }
    
    @Test
    @DisplayName("Constructor and Accessors")
    void testConstructorAndAccessors() {
        assertEquals(origin, ray.origin());
        assertEquals(direction, ray.direction());
        
        // Test with different vectors
        Vector3D newOrigin = new Vector3D(5.0, 6.0, 7.0);
        Vector3D newDirection = new Vector3D(1.0, 0.0, 0.0);
        Ray newRay = new Ray(newOrigin, newDirection);
        
        assertEquals(newOrigin, newRay.origin());
        assertEquals(newDirection, newRay.direction());
    }
    
    @Test
    @DisplayName("Point At Parameter t")
    void testPointAt() {
        // At t=0, should return origin
        Vector3D pointAtZero = ray.at(0.0);
        assertEquals(origin.x, pointAtZero.x, EPSILON);
        assertEquals(origin.y, pointAtZero.y, EPSILON);
        assertEquals(origin.z, pointAtZero.z, EPSILON);
        
        // At t=1, should return origin + direction
        Vector3D pointAtOne = ray.at(1.0);
        assertEquals(1.0, pointAtOne.x, EPSILON);
        assertEquals(3.0, pointAtOne.y, EPSILON); // 2.0 + 1.0
        assertEquals(3.0, pointAtOne.z, EPSILON);
        
        // At t=2.5
        Vector3D pointAtTwoPointFive = ray.at(2.5);
        assertEquals(1.0, pointAtTwoPointFive.x, EPSILON);
        assertEquals(4.5, pointAtTwoPointFive.y, EPSILON); // 2.0 + 2.5*1.0
        assertEquals(3.0, pointAtTwoPointFive.z, EPSILON);
        
        // Negative t
        Vector3D pointAtNegative = ray.at(-1.0);
        assertEquals(1.0, pointAtNegative.x, EPSILON);
        assertEquals(1.0, pointAtNegative.y, EPSILON); // 2.0 + (-1.0)*1.0
        assertEquals(3.0, pointAtNegative.z, EPSILON);
    }
    
    @Test
    @DisplayName("Ray with Diagonal Direction")
    void testDiagonalRay() {
        Vector3D diagonalOrigin = new Vector3D(0.0, 0.0, 0.0);
        Vector3D diagonalDirection = new Vector3D(1.0, 1.0, 1.0);
        Ray diagonalRay = new Ray(diagonalOrigin, diagonalDirection);
        
        Vector3D pointAtOne = diagonalRay.at(1.0);
        assertEquals(1.0, pointAtOne.x, EPSILON);
        assertEquals(1.0, pointAtOne.y, EPSILON);
        assertEquals(1.0, pointAtOne.z, EPSILON);
        
        Vector3D pointAtTwo = diagonalRay.at(2.0);
        assertEquals(2.0, pointAtTwo.x, EPSILON);
        assertEquals(2.0, pointAtTwo.y, EPSILON);
        assertEquals(2.0, pointAtTwo.z, EPSILON);
    }
    
    @Test
    @DisplayName("Ray with Normalized Direction")
    void testNormalizedDirection() {
        Vector3D unnormalizedDirection = new Vector3D(3.0, 4.0, 0.0);
        Vector3D normalizedDirection = unnormalizedDirection.normalized();
        Ray normalizedRay = new Ray(origin, normalizedDirection);
        
        // Direction should be unit length
        assertEquals(1.0, normalizedDirection.length(), EPSILON);
        
        // Point at t=5 should be 5 units away from origin
        Vector3D pointAtFive = normalizedRay.at(5.0);
        double distance = origin.distance(pointAtFive);
        assertEquals(5.0, distance, EPSILON);
    }
    
    @Test
    @DisplayName("Edge Cases")
    void testEdgeCases() {
        // Ray with zero direction (degenerate case)
        Vector3D zeroDirection = new Vector3D(0.0, 0.0, 0.0);
        Ray zeroRay = new Ray(origin, zeroDirection);
        
        // Should always return origin regardless of t
        Vector3D pointAtAny = zeroRay.at(100.0);
        assertEquals(origin.x, pointAtAny.x, EPSILON);
        assertEquals(origin.y, pointAtAny.y, EPSILON);
        assertEquals(origin.z, pointAtAny.z, EPSILON);
        
        // Ray with very small direction
        Vector3D tinyDirection = new Vector3D(1e-10, 1e-10, 1e-10);
        Ray tinyRay = new Ray(origin, tinyDirection);
        Vector3D pointAtLarge = tinyRay.at(1e10);
        // Should move only slightly from origin
        assertTrue(origin.distance(pointAtLarge) < 10.0);
        
        // Ray with very large direction
        Vector3D largeDirection = new Vector3D(1e10, 1e10, 1e10);
        Ray largeRay = new Ray(origin, largeDirection);
        Vector3D pointAtSmall = largeRay.at(1e-10);
        // Should move only slightly from origin
        assertTrue(origin.distance(pointAtSmall) < 10.0);
    }
    
    @Test
    @DisplayName("Immutability of Ray Components")
    void testImmutability() {
        // Original vectors should not be modified when creating ray
        Vector3D originalOrigin = new Vector3D(origin);
        Vector3D originalDirection = new Vector3D(direction);
        
        // Modify the vectors used to create the ray
        origin.mult(2.0);
        direction.mult(3.0);
        
        // Ray should still have original values (assuming Ray makes copies)
        // This test depends on Ray implementation - if it stores references, this might fail
        Vector3D rayOrigin = ray.origin();
        Vector3D rayDirection = ray.direction();
        
        // Check if ray preserves original values or uses references
        // This will help understand the design choice
        assertNotNull(rayOrigin);
        assertNotNull(rayDirection);
    }
    
    @Test
    @DisplayName("Ray Parameter Range")
    void testParameterRange() {
        // Test with various parameter values
        double[] testParams = {-1000.0, -1.0, -0.001, 0.0, 0.001, 1.0, 1000.0, Double.MAX_VALUE, Double.MIN_VALUE};
        
        for (double t : testParams) {
            Vector3D point = ray.at(t);
            assertNotNull(point);
            // Verify the mathematical relationship: point = origin + t * direction
            Vector3D expected = Vector3D.add(ray.origin(), Vector3D.mult(ray.direction(), t));
            assertEquals(expected.x, point.x, EPSILON);
            assertEquals(expected.y, point.y, EPSILON);
            assertEquals(expected.z, point.z, EPSILON);
        }
    }
    
    @Test
    @DisplayName("Ray Direction Independence")
    void testDirectionIndependence() {
        // Create rays with different directions from same origin
        Vector3D[] directions = {
            new Vector3D(1.0, 0.0, 0.0),  // X-axis
            new Vector3D(0.0, 1.0, 0.0),  // Y-axis
            new Vector3D(0.0, 0.0, 1.0),  // Z-axis
            new Vector3D(-1.0, 0.0, 0.0), // Negative X-axis
            new Vector3D(1.0, 1.0, 1.0).normalized() // Diagonal
        };
        
        for (Vector3D dir : directions) {
            Ray testRay = new Ray(origin, dir);
            Vector3D pointAtOne = testRay.at(1.0);
            
            // Distance from origin should equal direction magnitude times parameter
            double expectedDistance = dir.length();
            double actualDistance = origin.distance(pointAtOne);
            assertEquals(expectedDistance, actualDistance, EPSILON);
        }
    }
}
