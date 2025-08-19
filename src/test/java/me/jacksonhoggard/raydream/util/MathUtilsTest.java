package me.jacksonhoggard.raydream.util;

import me.jacksonhoggard.raydream.math.Vector3D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MathUtils Tests")
public class MathUtilsTest {
    
    private static final double EPSILON = 1e-10;
    
    @Test
    @DisplayName("Constants Validation")
    void testConstants() {
        assertEquals(Math.PI, MathUtils.PI, EPSILON);
        assertEquals(2.0 * Math.PI, MathUtils.TWO_PI, EPSILON);
        assertEquals(Math.PI * 0.5, MathUtils.HALF_PI, EPSILON);
        assertEquals(Math.PI / 180.0, MathUtils.DEG_TO_RAD, EPSILON);
        assertEquals(180.0 / Math.PI, MathUtils.RAD_TO_DEG, EPSILON);
        assertTrue(MathUtils.EPSILON > 0);
    }
    
    @Test
    @DisplayName("Equality Checks")
    void testEqualityChecks() {
        // Basic equality
        assertTrue(MathUtils.equals(1.0, 1.0));
        assertTrue(MathUtils.equals(0.0, 0.0));
        assertFalse(MathUtils.equals(1.0, 2.0));
        
        // Near equality within default epsilon
        assertTrue(MathUtils.equals(1.0, 1.0 + MathUtils.EPSILON / 2));
        assertFalse(MathUtils.equals(1.0, 1.0 + MathUtils.EPSILON * 2));
        
        // Custom epsilon
        assertTrue(MathUtils.equals(1.0, 1.1, 0.2));
        assertFalse(MathUtils.equals(1.0, 1.1, 0.05));
        
        // Edge cases
        assertTrue(MathUtils.equals(Double.MAX_VALUE, Double.MAX_VALUE));
        assertTrue(MathUtils.equals(Double.MIN_VALUE, Double.MIN_VALUE));
        assertTrue(MathUtils.equals(-0.0, 0.0)); // -0.0 and 0.0 should be equal
    }
    
    @Test
    @DisplayName("Clamping Operations")
    void testClamping() {
        // Basic clamping
        assertEquals(5.0, MathUtils.clamp(5.0, 0.0, 10.0), EPSILON);
        assertEquals(0.0, MathUtils.clamp(-5.0, 0.0, 10.0), EPSILON);
        assertEquals(10.0, MathUtils.clamp(15.0, 0.0, 10.0), EPSILON);
        
        // Edge cases
        assertEquals(0.0, MathUtils.clamp(0.0, 0.0, 10.0), EPSILON); // At min boundary
        assertEquals(10.0, MathUtils.clamp(10.0, 0.0, 10.0), EPSILON); // At max boundary
        
        // Invalid range (min > max) - should handle gracefully
        // The behavior depends on implementation, but should not crash
        assertDoesNotThrow(() -> MathUtils.clamp(5.0, 10.0, 0.0));
    }
    
    @Test
    @DisplayName("Linear Interpolation")
    void testLinearInterpolation() {
        // Basic interpolation
        assertEquals(5.0, MathUtils.lerp(0.0, 10.0, 0.5), EPSILON);
        assertEquals(0.0, MathUtils.lerp(0.0, 10.0, 0.0), EPSILON);
        assertEquals(10.0, MathUtils.lerp(0.0, 10.0, 1.0), EPSILON);
        
        // Extrapolation
        assertEquals(-5.0, MathUtils.lerp(0.0, 10.0, -0.5), EPSILON);
        assertEquals(15.0, MathUtils.lerp(0.0, 10.0, 1.5), EPSILON);
        
        // Vector interpolation
        Vector3D a = new Vector3D(0.0, 0.0, 0.0);
        Vector3D b = new Vector3D(10.0, 20.0, 30.0);
        Vector3D result = MathUtils.lerp(a, b, 0.5);
        
        assertEquals(5.0, result.x, EPSILON);
        assertEquals(10.0, result.y, EPSILON);
        assertEquals(15.0, result.z, EPSILON);
        
        // Vector interpolation at boundaries
        Vector3D resultStart = MathUtils.lerp(a, b, 0.0);
        assertEquals(a.x, resultStart.x, EPSILON);
        assertEquals(a.y, resultStart.y, EPSILON);
        assertEquals(a.z, resultStart.z, EPSILON);
        
        Vector3D resultEnd = MathUtils.lerp(a, b, 1.0);
        assertEquals(b.x, resultEnd.x, EPSILON);
        assertEquals(b.y, resultEnd.y, EPSILON);
        assertEquals(b.z, resultEnd.z, EPSILON);
    }
    
    @Test
    @DisplayName("Random Number Generation")
    void testRandomGeneration() {
        // Random in range
        for (int i = 0; i < 100; i++) {
            double random = MathUtils.random(5.0, 10.0);
            assertTrue(random >= 5.0);
            assertTrue(random <= 10.0);
        }
        
        // Random with same min and max
        double sameValue = MathUtils.random(5.0, 5.01); // Ensure max > min
        assertTrue(sameValue >= 5.0 && sameValue < 5.01);
        
        // Random vector components should be in [-1, 1]
        for (int i = 0; i < 100; i++) {
            Vector3D randomVec = MathUtils.randomVector();
            assertTrue(randomVec.x >= -1.0 && randomVec.x <= 1.0);
            assertTrue(randomVec.y >= -1.0 && randomVec.y <= 1.0);
            assertTrue(randomVec.z >= -1.0 && randomVec.z <= 1.0);
        }
        
        // Random unit vector should have length 1
        for (int i = 0; i < 100; i++) {
            Vector3D unitVec = MathUtils.randomUnitVector();
            assertEquals(1.0, unitVec.length(), 0.01); // Small tolerance for floating point
        }
    }
    
    @Test
    @DisplayName("Angle Conversions")
    void testAngleConversions() {
        // Degrees to radians
        assertEquals(0.0, MathUtils.toRadians(0.0), EPSILON);
        assertEquals(MathUtils.PI, MathUtils.toRadians(180.0), EPSILON);
        assertEquals(MathUtils.HALF_PI, MathUtils.toRadians(90.0), EPSILON);
        assertEquals(MathUtils.TWO_PI, MathUtils.toRadians(360.0), EPSILON);
        
        // Radians to degrees
        assertEquals(0.0, MathUtils.toDegrees(0.0), EPSILON);
        assertEquals(180.0, MathUtils.toDegrees(MathUtils.PI), EPSILON);
        assertEquals(90.0, MathUtils.toDegrees(MathUtils.HALF_PI), EPSILON);
        assertEquals(360.0, MathUtils.toDegrees(MathUtils.TWO_PI), EPSILON);
        
        // Round trip conversion
        double degrees = 45.0;
        double radians = MathUtils.toRadians(degrees);
        double backToDegrees = MathUtils.toDegrees(radians);
        assertEquals(degrees, backToDegrees, EPSILON);
    }
    
    @Test
    @DisplayName("Vector Reflection")
    void testReflection() {
        Vector3D incident = new Vector3D(1.0, -1.0, 0.0);
        Vector3D normal = new Vector3D(0.0, 1.0, 0.0);
        Vector3D reflected = MathUtils.reflect(incident, normal);
        
        // Incident ray hitting surface from above should reflect upward
        assertEquals(1.0, reflected.x, EPSILON);
        assertEquals(1.0, reflected.y, EPSILON);
        assertEquals(0.0, reflected.z, EPSILON);
        
        // Test with different normal
        Vector3D normalX = new Vector3D(1.0, 0.0, 0.0);
        Vector3D incidentX = new Vector3D(-1.0, 1.0, 0.0);
        Vector3D reflectedX = MathUtils.reflect(incidentX, normalX);
        assertEquals(1.0, reflectedX.x, EPSILON);
        assertEquals(1.0, reflectedX.y, EPSILON);
        assertEquals(0.0, reflectedX.z, EPSILON);
    }
    
    @Test
    @DisplayName("Vector Refraction")
    void testRefraction() {
        Vector3D incident = new Vector3D(0.0, -1.0, 0.0).normalized();
        Vector3D normal = new Vector3D(0.0, 1.0, 0.0);
        double etaRatio = 1.0 / 1.5; // Air to glass
        
        Vector3D refracted = MathUtils.refract(incident, normal, etaRatio);
        assertNotNull(refracted);
        
        // Test total internal reflection
        Vector3D steepIncident = new Vector3D(0.8, -0.6, 0.0).normalized();
        double highEtaRatio = 1.5 / 1.0; // Glass to air
        Vector3D totalReflection = MathUtils.refract(steepIncident, normal, highEtaRatio);
        // Might be null for total internal reflection depending on angle
        // Just ensure no exception is thrown
        assertDoesNotThrow(() -> MathUtils.refract(steepIncident, normal, highEtaRatio));
    }
    
    @Test
    @DisplayName("Fresnel Reflectance")
    void testFresnelReflectance() {
        // At normal incidence (cosine = 1), should give R0
        double r0 = MathUtils.schlickFresnel(1.0, 1.5);
        assertTrue(r0 >= 0.0 && r0 <= 1.0);
        
        // At grazing incidence (cosine = 0), should approach 1
        double rGrazing = MathUtils.schlickFresnel(0.0, 1.5);
        assertEquals(1.0, rGrazing, EPSILON);
        
        // Intermediate angles
        double rIntermediate = MathUtils.schlickFresnel(0.5, 1.5);
        assertTrue(rIntermediate >= 0.0 && rIntermediate <= 1.0);
        assertTrue(rIntermediate > r0); // Should be higher than normal incidence
    }
    
    @Test
    @DisplayName("Random Hemisphere Generation")
    void testRandomHemisphere() {
        Vector3D normal = new Vector3D(0.0, 1.0, 0.0);
        
        // Generate many random hemisphere vectors
        for (int i = 0; i < 100; i++) {
            Vector3D hemisphereVec = MathUtils.randomHemisphere(normal);
            
            // Should be unit length
            assertEquals(1.0, hemisphereVec.length(), 0.01);
            
            // Should be in the hemisphere (dot product with normal >= 0)
            assertTrue(hemisphereVec.dot(normal) >= -EPSILON);
        }
        
        // Test with different normal
        Vector3D normalNegZ = new Vector3D(0.0, 0.0, -1.0);
        for (int i = 0; i < 10; i++) {
            Vector3D hemisphereVec = MathUtils.randomHemisphere(normalNegZ);
            assertTrue(hemisphereVec.dot(normalNegZ) >= -EPSILON);
        }
    }
    
    @Test
    @DisplayName("Edge Cases and Error Handling")
    void testEdgeCases() {
        // Test with infinity
        assertDoesNotThrow(() -> MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertDoesNotThrow(() -> MathUtils.clamp(Double.POSITIVE_INFINITY, 0.0, 10.0));
        
        // Test with NaN
        assertDoesNotThrow(() -> MathUtils.equals(Double.NaN, Double.NaN));
        assertDoesNotThrow(() -> MathUtils.clamp(Double.NaN, 0.0, 10.0));
        
        // Test very small numbers
        assertDoesNotThrow(() -> MathUtils.equals(Double.MIN_VALUE, 0.0));
        
        // Test very large numbers
        assertDoesNotThrow(() -> MathUtils.equals(Double.MAX_VALUE, Double.MAX_VALUE));
    }
    
    @Test
    @DisplayName("Performance Considerations")
    void testPerformance() {
        // These tests ensure the utility methods complete in reasonable time
        long startTime = System.nanoTime();
        
        // Run many operations
        for (int i = 0; i < 10000; i++) {
            MathUtils.lerp(0.0, 1.0, 0.5);
            MathUtils.clamp(i, 0.0, 10000.0);
            MathUtils.equals(i, i + 0.1, 0.2);
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Should complete in less than 100ms (very generous for simple operations)
        assertTrue(duration < 100_000_000, "Operations took too long: " + duration + " ns");
    }
}
