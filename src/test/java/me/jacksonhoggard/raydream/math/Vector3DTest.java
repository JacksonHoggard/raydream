package me.jacksonhoggard.raydream.math;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Vector3D Tests")
public class Vector3DTest {
    
    private Vector3D v1, v2, v3;
    private static final double EPSILON = 1e-10;
    
    @BeforeEach
    void setUp() {
        v1 = new Vector3D(1.0, 2.0, 3.0);
        v2 = new Vector3D(4.0, 5.0, 6.0);
        v3 = new Vector3D(0.0, 0.0, 0.0);
    }
    
    @Test
    @DisplayName("Constructor Tests")
    void testConstructors() {
        // Default constructor
        Vector3D defaultVec = new Vector3D();
        assertEquals(0.0, defaultVec.x, EPSILON);
        assertEquals(0.0, defaultVec.y, EPSILON);
        assertEquals(0.0, defaultVec.z, EPSILON);
        
        // Parameterized constructor
        Vector3D paramVec = new Vector3D(1.5, 2.5, 3.5);
        assertEquals(1.5, paramVec.x, EPSILON);
        assertEquals(2.5, paramVec.y, EPSILON);
        assertEquals(3.5, paramVec.z, EPSILON);
        
        // Copy constructor
        Vector3D copyVec = new Vector3D(v1);
        assertEquals(v1.x, copyVec.x, EPSILON);
        assertEquals(v1.y, copyVec.y, EPSILON);
        assertEquals(v1.z, copyVec.z, EPSILON);
        assertNotSame(v1, copyVec); // Ensure it's a different object
    }
    
    @Test
    @DisplayName("Set Operations")
    void testSetOperations() {
        Vector3D testVec = new Vector3D();
        
        // Set from vector
        testVec.set(v1);
        assertEquals(v1.x, testVec.x, EPSILON);
        assertEquals(v1.y, testVec.y, EPSILON);
        assertEquals(v1.z, testVec.z, EPSILON);
        
        // Set from coordinates
        testVec.set(7.0, 8.0, 9.0);
        assertEquals(7.0, testVec.x, EPSILON);
        assertEquals(8.0, testVec.y, EPSILON);
        assertEquals(9.0, testVec.z, EPSILON);
    }
    
    @Test
    @DisplayName("Negation Operations")
    void testNegation() {
        // In-place negation
        Vector3D negVec = new Vector3D(v1);
        negVec.negate();
        assertEquals(-1.0, negVec.x, EPSILON);
        assertEquals(-2.0, negVec.y, EPSILON);
        assertEquals(-3.0, negVec.z, EPSILON);
        
        // Non-destructive negation
        Vector3D negatedVec = v1.negated();
        assertEquals(-1.0, negatedVec.x, EPSILON);
        assertEquals(-2.0, negatedVec.y, EPSILON);
        assertEquals(-3.0, negatedVec.z, EPSILON);
        
        // Original should be unchanged
        assertEquals(1.0, v1.x, EPSILON);
        assertEquals(2.0, v1.y, EPSILON);
        assertEquals(3.0, v1.z, EPSILON);
    }
    
    @Test
    @DisplayName("Addition Operations")
    void testAddition() {
        // In-place addition
        Vector3D addVec = new Vector3D(v1);
        addVec.add(v2);
        assertEquals(5.0, addVec.x, EPSILON);
        assertEquals(7.0, addVec.y, EPSILON);
        assertEquals(9.0, addVec.z, EPSILON);
        
        // Static addition
        Vector3D result = Vector3D.add(v1, v2);
        assertEquals(5.0, result.x, EPSILON);
        assertEquals(7.0, result.y, EPSILON);
        assertEquals(9.0, result.z, EPSILON);
        
        // Original vectors should be unchanged
        assertEquals(1.0, v1.x, EPSILON);
        assertEquals(4.0, v2.x, EPSILON);
    }
    
    @Test
    @DisplayName("Subtraction Operations")
    void testSubtraction() {
        // In-place subtraction
        Vector3D subVec = new Vector3D(v2);
        subVec.sub(v1);
        assertEquals(3.0, subVec.x, EPSILON);
        assertEquals(3.0, subVec.y, EPSILON);
        assertEquals(3.0, subVec.z, EPSILON);
        
        // Static subtraction
        Vector3D result = Vector3D.sub(v2, v1);
        assertEquals(3.0, result.x, EPSILON);
        assertEquals(3.0, result.y, EPSILON);
        assertEquals(3.0, result.z, EPSILON);
    }
    
    @Test
    @DisplayName("Multiplication Operations")
    void testMultiplication() {
        // Vector multiplication
        Vector3D multVec = new Vector3D(v1);
        multVec.mult(v2);
        assertEquals(4.0, multVec.x, EPSILON);
        assertEquals(10.0, multVec.y, EPSILON);
        assertEquals(18.0, multVec.z, EPSILON);
        
        // Scalar multiplication
        Vector3D scalarMult = new Vector3D(v1);
        scalarMult.mult(2.0);
        assertEquals(2.0, scalarMult.x, EPSILON);
        assertEquals(4.0, scalarMult.y, EPSILON);
        assertEquals(6.0, scalarMult.z, EPSILON);
        
        // Static scalar multiplication
        Vector3D result1 = Vector3D.mult(v1, 3.0);
        Vector3D result2 = Vector3D.mult(3.0, v1);
        assertEquals(3.0, result1.x, EPSILON);
        assertEquals(3.0, result2.x, EPSILON);
    }
    
    @Test
    @DisplayName("Division Operations")
    void testDivision() {
        Vector3D divVec = new Vector3D(2.0, 4.0, 6.0);
        divVec.div(2.0);
        assertEquals(1.0, divVec.x, EPSILON);
        assertEquals(2.0, divVec.y, EPSILON);
        assertEquals(3.0, divVec.z, EPSILON);
        
        // Static division
        Vector3D result = Vector3D.div(new Vector3D(6.0, 9.0, 12.0), 3.0);
        assertEquals(2.0, result.x, EPSILON);
        assertEquals(3.0, result.y, EPSILON);
        assertEquals(4.0, result.z, EPSILON);
    }
    
    @Test
    @DisplayName("Length Calculation")
    void testLength() {
        Vector3D unitVec = new Vector3D(1.0, 0.0, 0.0);
        assertEquals(1.0, unitVec.length(), EPSILON);
        
        Vector3D vec345 = new Vector3D(3.0, 4.0, 0.0);
        assertEquals(5.0, vec345.length(), EPSILON);
        
        // Zero vector
        assertEquals(0.0, v3.length(), EPSILON);
    }
    
    @Test
    @DisplayName("Dot Product")
    void testDotProduct() {
        double dot = v1.dot(v2);
        assertEquals(32.0, dot, EPSILON); // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        
        // Dot product with zero vector
        assertEquals(0.0, v1.dot(v3), EPSILON);
        
        // Dot product with itself (squared length)
        assertEquals(14.0, v1.dot(v1), EPSILON); // 1² + 2² + 3² = 1 + 4 + 9 = 14
    }
    
    @Test
    @DisplayName("Cross Product")
    void testCrossProduct() {
        Vector3D i = new Vector3D(1.0, 0.0, 0.0);
        Vector3D j = new Vector3D(0.0, 1.0, 0.0);
        Vector3D k = new Vector3D(0.0, 0.0, 1.0);
        
        // i × j = k
        Vector3D result = i.cross(j);
        assertEquals(0.0, result.x, EPSILON);
        assertEquals(0.0, result.y, EPSILON);
        assertEquals(1.0, result.z, EPSILON);
        
        // j × k = i
        result = j.cross(k);
        assertEquals(1.0, result.x, EPSILON);
        assertEquals(0.0, result.y, EPSILON);
        assertEquals(0.0, result.z, EPSILON);
        
        // Cross product with itself should be zero
        result = v1.cross(v1);
        assertEquals(0.0, result.x, EPSILON);
        assertEquals(0.0, result.y, EPSILON);
        assertEquals(0.0, result.z, EPSILON);
    }
    
    @Test
    @DisplayName("Normalization")
    void testNormalization() {
        Vector3D normalizedVec = new Vector3D(v1);
        normalizedVec.normalize();
        assertEquals(1.0, normalizedVec.length(), EPSILON);
        
        // Non-destructive normalization
        Vector3D normalized = v1.normalized();
        assertEquals(1.0, normalized.length(), EPSILON);
        
        // Original should be unchanged
        assertNotEquals(1.0, v1.length(), EPSILON);
    }
    
    @Test
    @DisplayName("Distance Calculation")
    void testDistance() {
        Vector3D origin = new Vector3D(0.0, 0.0, 0.0);
        Vector3D point = new Vector3D(3.0, 4.0, 0.0);
        
        double distance = origin.distance(point);
        assertEquals(5.0, distance, EPSILON);
        
        // Distance from point to itself
        assertEquals(0.0, v1.distance(v1), EPSILON);
    }
    
    @Test
    @DisplayName("Array Conversion")
    void testToArray() {
        double[] array = v1.toArray();
        assertEquals(3, array.length);
        assertEquals(1.0, array[0], EPSILON);
        assertEquals(2.0, array[1], EPSILON);
        assertEquals(3.0, array[2], EPSILON);
    }
    
    @Test
    @DisplayName("String Representation")
    void testToString() {
        String str = v1.toString();
        assertTrue(str.contains("1.0"));
        assertTrue(str.contains("2.0"));
        assertTrue(str.contains("3.0"));
    }
    
    @Test
    @DisplayName("Equality")
    void testEquals() {
        Vector3D identical = new Vector3D(1.0, 2.0, 3.0);
        Vector3D different = new Vector3D(1.0, 2.0, 4.0);
        
        assertTrue(v1.equals(identical));
        assertFalse(v1.equals(different));
        assertTrue(v1.equals(v1)); // Reflexivity
    }
    
    @Test
    @DisplayName("Edge Cases")
    void testEdgeCases() {
        // Division by zero (Vector3D.div method uses 1/t, so may return Infinity)
        Vector3D testVec = new Vector3D(1.0, 1.0, 1.0);
        Vector3D result = testVec.div(0.0);
        // Should handle gracefully, likely returning infinity values
        assertTrue(Double.isInfinite(result.x) || Double.isNaN(result.x));
        
        // Very small numbers
        Vector3D smallVec = new Vector3D(1e-100, 1e-100, 1e-100);
        assertNotEquals(0.0, smallVec.length());
        
        // Very large numbers
        Vector3D largeVec = new Vector3D(1e100, 1e100, 1e100);
        assertTrue(largeVec.length() > 1e100);
    }
}
