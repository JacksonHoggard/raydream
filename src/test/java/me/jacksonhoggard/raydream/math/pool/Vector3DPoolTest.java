package me.jacksonhoggard.raydream.math.pool;

import me.jacksonhoggard.raydream.math.Vector3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Vector3DPool Tests")
public class Vector3DPoolTest {
    
    private static final double EPSILON = 1e-10;
    
    @BeforeEach
    void setUp() {
        // Clear any existing vectors in the pool by borrowing many
        for (int i = 0; i < 2000; i++) {
            Vector3DPool.borrow();
        }
    }
    
    @Test
    @DisplayName("Basic Borrow and Release")
    void testBasicBorrowAndRelease() {
        // Borrow a vector
        Vector3D vector = Vector3DPool.borrow();
        assertNotNull(vector);
        
        // Set some values
        vector.set(1.0, 2.0, 3.0);
        assertEquals(1.0, vector.x, EPSILON);
        assertEquals(2.0, vector.y, EPSILON);
        assertEquals(3.0, vector.z, EPSILON);
        
        // Release it back to pool
        Vector3DPool.release(vector);
        
        // Borrow again - might get the same instance
        Vector3D vector2 = Vector3DPool.borrow();
        assertNotNull(vector2);
        
        // The vector might contain arbitrary values after reuse
        // This is expected behavior according to the documentation
    }
    
    @Test
    @DisplayName("Borrow with Initial Values")
    void testBorrowWithValues() {
        Vector3D vector = Vector3DPool.borrow(5.0, 6.0, 7.0);
        assertNotNull(vector);
        assertEquals(5.0, vector.x, EPSILON);
        assertEquals(6.0, vector.y, EPSILON);
        assertEquals(7.0, vector.z, EPSILON);
        
        Vector3DPool.release(vector);
    }
    
    @Test
    @DisplayName("Multiple Borrow Operations")
    void testMultipleBorrow() {
        // Borrow multiple vectors
        Vector3D[] vectors = new Vector3D[10];
        for (int i = 0; i < vectors.length; i++) {
            vectors[i] = Vector3DPool.borrow();
            assertNotNull(vectors[i]);
            vectors[i].set(i, i * 2, i * 3);
        }
        
        // Verify all vectors have correct values
        for (int i = 0; i < vectors.length; i++) {
            assertEquals(i, vectors[i].x, EPSILON);
            assertEquals(i * 2, vectors[i].y, EPSILON);
            assertEquals(i * 3, vectors[i].z, EPSILON);
        }
        
        // Release all vectors
        for (Vector3D vector : vectors) {
            Vector3DPool.release(vector);
        }
    }
    
    @Test
    @DisplayName("Pool Reuse")
    void testPoolReuse() {
        // Borrow and release a vector
        Vector3D vector1 = Vector3DPool.borrow();
        vector1.set(1.0, 1.0, 1.0);
        Vector3DPool.release(vector1);
        
        // Borrow another vector - this might be the same instance
        Vector3D vector2 = Vector3DPool.borrow();
        assertNotNull(vector2);
        
        // Set new values to ensure it works regardless of previous state
        vector2.set(2.0, 2.0, 2.0);
        assertEquals(2.0, vector2.x, EPSILON);
        assertEquals(2.0, vector2.y, EPSILON);
        assertEquals(2.0, vector2.z, EPSILON);
        
        Vector3DPool.release(vector2);
    }
    
    @Test
    @DisplayName("Null Release Handling")
    void testNullRelease() {
        // Releasing null should not cause issues
        Vector3D nullVector = null;
        assertDoesNotThrow(() -> Vector3DPool.release(nullVector));
        
        // Pool should still work normally after null release
        Vector3D vector = Vector3DPool.borrow();
        assertNotNull(vector);
        Vector3DPool.release(vector);
    }
    
    @Test
    @DisplayName("Large Scale Operations")
    void testLargeScaleOperations() {
        int numOperations = 1000;
        Vector3D[] vectors = new Vector3D[numOperations];
        
        // Borrow many vectors
        for (int i = 0; i < numOperations; i++) {
            vectors[i] = Vector3DPool.borrow(i, i * 2, i * 3);
            assertNotNull(vectors[i]);
        }
        
        // Verify a sample of vectors
        assertEquals(0.0, vectors[0].x, EPSILON);
        assertEquals(50.0, vectors[50].x, EPSILON);
        assertEquals(999.0, vectors[999].x, EPSILON);
        
        // Release all vectors
        for (Vector3D vector : vectors) {
            Vector3DPool.release(vector);
        }
        
        // Borrow again to test pool replenishment
        Vector3D testVector = Vector3DPool.borrow();
        assertNotNull(testVector);
        Vector3DPool.release(testVector);
    }
    
    @Test
    @DisplayName("Concurrent Access Simulation")
    void testConcurrentAccessSimulation() {
        // Simulate concurrent access by rapidly borrowing and releasing
        // This tests thread safety aspects (though not actual concurrency)
        
        for (int round = 0; round < 100; round++) {
            Vector3D[] tempVectors = new Vector3D[10];
            
            // Rapid borrow
            for (int i = 0; i < tempVectors.length; i++) {
                tempVectors[i] = Vector3DPool.borrow();
                assertNotNull(tempVectors[i]);
            }
            
            // Rapid release
            for (Vector3D vector : tempVectors) {
                Vector3DPool.release(vector);
            }
        }
    }
    
    @Test
    @DisplayName("Pool Performance")
    void testPoolPerformance() {
        int numOperations = 10000;
        long startTime = System.nanoTime();
        
        // Perform many borrow/release cycles
        for (int i = 0; i < numOperations; i++) {
            Vector3D vector = Vector3DPool.borrow();
            vector.set(i, i, i); // Do some work with the vector
            Vector3DPool.release(vector);
        }
        
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        
        // Pool operations should be very fast
        assertTrue(duration < 100_000_000, "Pool operations took too long: " + duration + " ns");
    }
    
    @Test
    @DisplayName("Vector Independence")
    void testVectorIndependence() {
        // Borrow multiple vectors and ensure they are independent
        Vector3D vector1 = Vector3DPool.borrow(1.0, 1.0, 1.0);
        Vector3D vector2 = Vector3DPool.borrow(2.0, 2.0, 2.0);
        Vector3D vector3 = Vector3DPool.borrow(3.0, 3.0, 3.0);
        
        // Modify one vector
        vector1.mult(10.0);
        
        // Other vectors should be unchanged
        assertEquals(2.0, vector2.x, EPSILON);
        assertEquals(2.0, vector2.y, EPSILON);
        assertEquals(2.0, vector2.z, EPSILON);
        
        assertEquals(3.0, vector3.x, EPSILON);
        assertEquals(3.0, vector3.y, EPSILON);
        assertEquals(3.0, vector3.z, EPSILON);
        
        // Release all
        Vector3DPool.release(vector1);
        Vector3DPool.release(vector2);
        Vector3DPool.release(vector3);
    }
    
    @Test
    @DisplayName("Pool State Consistency")
    void testPoolStateConsistency() {
        // Test that the pool maintains consistency across operations
        Vector3D[] firstBatch = new Vector3D[50];
        Vector3D[] secondBatch = new Vector3D[50];
        
        // First batch
        for (int i = 0; i < firstBatch.length; i++) {
            firstBatch[i] = Vector3DPool.borrow();
            firstBatch[i].set(i, 0, 0);
        }
        
        // Release first batch
        for (Vector3D vector : firstBatch) {
            Vector3DPool.release(vector);
        }
        
        // Second batch - might reuse some vectors from first batch
        for (int i = 0; i < secondBatch.length; i++) {
            secondBatch[i] = Vector3DPool.borrow();
            assertNotNull(secondBatch[i]);
            // Set new values regardless of previous state
            secondBatch[i].set(i * 10, i * 20, i * 30);
        }
        
        // Verify second batch values
        assertEquals(0.0, secondBatch[0].x, EPSILON);
        assertEquals(100.0, secondBatch[10].x, EPSILON);
        
        // Release second batch
        for (Vector3D vector : secondBatch) {
            Vector3DPool.release(vector);
        }
    }
    
    @Test
    @DisplayName("Edge Cases")
    void testEdgeCases() {
        // Test with extreme values
        Vector3D extremeVector = Vector3DPool.borrow(Double.MAX_VALUE, Double.MIN_VALUE, Double.NEGATIVE_INFINITY);
        assertEquals(Double.MAX_VALUE, extremeVector.x, 0.0);
        assertEquals(Double.MIN_VALUE, extremeVector.y, 0.0);
        assertEquals(Double.NEGATIVE_INFINITY, extremeVector.z, 0.0);
        Vector3DPool.release(extremeVector);
        
        // Test with NaN
        Vector3D nanVector = Vector3DPool.borrow(Double.NaN, 0.0, 0.0);
        assertTrue(Double.isNaN(nanVector.x));
        assertEquals(0.0, nanVector.y, EPSILON);
        Vector3DPool.release(nanVector);
        
        // Test release of same vector multiple times (should be handled gracefully)
        Vector3D vector = Vector3DPool.borrow();
        Vector3DPool.release(vector);
        assertDoesNotThrow(() -> Vector3DPool.release(vector)); // Should not cause issues
    }
}
