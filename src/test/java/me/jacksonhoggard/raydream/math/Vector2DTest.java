package me.jacksonhoggard.raydream.math;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Vector2D Tests")
public class Vector2DTest {
    
    private Vector2D v1, v2, v3;
    private static final double EPSILON = 1e-10;
    
    @BeforeEach
    void setUp() {
        v1 = new Vector2D(1.0, 2.0);
        v2 = new Vector2D(3.0, 4.0);
        v3 = new Vector2D(0.0, 0.0);
    }
    
    @Test
    @DisplayName("Constructor Tests")
    void testConstructors() {
        // Default constructor
        Vector2D defaultVec = new Vector2D();
        assertEquals(0.0, defaultVec.x, EPSILON);
        assertEquals(0.0, defaultVec.y, EPSILON);
        
        // Parameterized constructor
        Vector2D paramVec = new Vector2D(1.5, 2.5);
        assertEquals(1.5, paramVec.x, EPSILON);
        assertEquals(2.5, paramVec.y, EPSILON);
        
        // Copy constructor
        Vector2D copyVec = new Vector2D(v1);
        assertEquals(v1.x, copyVec.x, EPSILON);
        assertEquals(v1.y, copyVec.y, EPSILON);
        assertNotSame(v1, copyVec);
    }
    
    @Test
    @DisplayName("Set Operations")
    void testSetOperations() {
        Vector2D testVec = new Vector2D();
        
        // Set from vector
        testVec.set(v1);
        assertEquals(v1.x, testVec.x, EPSILON);
        assertEquals(v1.y, testVec.y, EPSILON);
        
        // Set from coordinates
        testVec.set(5.0, 6.0);
        assertEquals(5.0, testVec.x, EPSILON);
        assertEquals(6.0, testVec.y, EPSILON);
    }
    
    @Test
    @DisplayName("Basic Arithmetic Operations")
    void testArithmeticOperations() {
        // Addition
        Vector2D addResult = new Vector2D(v1);
        addResult.add(v2);
        assertEquals(4.0, addResult.x, EPSILON);
        assertEquals(6.0, addResult.y, EPSILON);
        
        // Subtraction
        Vector2D subResult = new Vector2D(v2);
        subResult.sub(v1);
        assertEquals(2.0, subResult.x, EPSILON);
        assertEquals(2.0, subResult.y, EPSILON);
        
        // Scalar multiplication
        Vector2D multResult = new Vector2D(v1);
        multResult.mult(2.0);
        assertEquals(2.0, multResult.x, EPSILON);
        assertEquals(4.0, multResult.y, EPSILON);
    }
    
    @Test
    @DisplayName("Static Operations")
    void testStaticOperations() {
        // Static addition
        Vector2D addResult = Vector2D.add(v1, v2);
        assertEquals(4.0, addResult.x, EPSILON);
        assertEquals(6.0, addResult.y, EPSILON);
        
        // Static subtraction
        Vector2D subResult = Vector2D.sub(v2, v1);
        assertEquals(2.0, subResult.x, EPSILON);
        assertEquals(2.0, subResult.y, EPSILON);
        
        // Static multiplication
        Vector2D multResult = Vector2D.mult(v1, 3.0);
        assertEquals(3.0, multResult.x, EPSILON);
        assertEquals(6.0, multResult.y, EPSILON);
    }
    
    @Test
    @DisplayName("Length and Distance")
    void testLengthAndDistance() {
        // Length calculation
        Vector2D unitVec = new Vector2D(1.0, 0.0);
        assertEquals(1.0, unitVec.length(), EPSILON);
        
        Vector2D vec34 = new Vector2D(3.0, 4.0);
        assertEquals(5.0, vec34.length(), EPSILON);
        
        // Distance calculation
        Vector2D origin = new Vector2D(0.0, 0.0);
        Vector2D point = new Vector2D(3.0, 4.0);
        assertEquals(5.0, origin.distance(point), EPSILON);
    }
    
    @Test
    @DisplayName("Dot Product")
    void testDotProduct() {
        double dot = v1.dot(v2);
        assertEquals(11.0, dot, EPSILON); // 1*3 + 2*4 = 3 + 8 = 11
        
        // Dot product with zero vector
        assertEquals(0.0, v1.dot(v3), EPSILON);
    }
    
    @Test
    @DisplayName("Normalization")
    void testNormalization() {
        Vector2D vec = new Vector2D(3.0, 4.0);
        vec.normalize();
        assertEquals(1.0, vec.length(), EPSILON);
        
        // Non-destructive normalization
        Vector2D original = new Vector2D(5.0, 12.0);
        Vector2D normalized = original.normalized();
        assertEquals(1.0, normalized.length(), EPSILON);
        assertEquals(13.0, original.length(), EPSILON); // Original unchanged
    }
    
    @Test
    @DisplayName("Negation")
    void testNegation() {
        Vector2D negVec = new Vector2D(v1);
        negVec.negate();
        assertEquals(-1.0, negVec.x, EPSILON);
        assertEquals(-2.0, negVec.y, EPSILON);
        
        // Non-destructive negation
        Vector2D negated = v1.negated();
        assertEquals(-1.0, negated.x, EPSILON);
        assertEquals(-2.0, negated.y, EPSILON);
        assertEquals(1.0, v1.x, EPSILON); // Original unchanged
    }
    
    @Test
    @DisplayName("Edge Cases")
    void testEdgeCases() {
        // Zero vector normalization (Vector2D may handle this differently)
        Vector2D zeroVec = new Vector2D(0.0, 0.0);
        Vector2D normalizedZero = zeroVec.normalized();
        // May return NaN or zero vector depending on implementation
        assertTrue(Double.isNaN(normalizedZero.x) || normalizedZero.x == 0.0);
        
        // Very small vector
        Vector2D smallVec = new Vector2D(1e-100, 1e-100);
        assertTrue(smallVec.length() > 0);
        
        // Very large vector
        Vector2D largeVec = new Vector2D(1e100, 1e100);
        assertTrue(largeVec.length() > 1e100);
    }
    
    @Test
    @DisplayName("Equality and String Representation")
    void testEqualityAndString() {
        Vector2D identical = new Vector2D(1.0, 2.0);
        Vector2D different = new Vector2D(1.0, 3.0);
        
        assertTrue(v1.equals(identical));
        assertFalse(v1.equals(different));
        
        // String representation should contain coordinates
        String str = v1.toString();
        assertTrue(str.contains("1.0"));
        assertTrue(str.contains("2.0"));
    }
}
