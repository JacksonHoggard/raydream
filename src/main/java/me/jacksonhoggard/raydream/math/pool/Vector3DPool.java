package me.jacksonhoggard.raydream.math.pool;

import me.jacksonhoggard.raydream.math.Vector3D;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Object pool for Vector3D instances to reduce garbage collection during rendering.
 * This is a major performance optimization for raytracing operations.
 */
public class Vector3DPool {

    private static final int INITIAL_POOL_SIZE = 1000;
    private static final ConcurrentLinkedQueue<Vector3D> pool = new ConcurrentLinkedQueue<>();

    static {
        // Pre-populate the pool
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            pool.offer(new Vector3D());
        }
    }

    /**
     * Borrows a Vector3D from the pool. The vector may contain arbitrary values.
     * @return a Vector3D instance from the pool
     */
    public static Vector3D borrow() {
        Vector3D vector = pool.poll();
        if (vector == null) {
            vector = new Vector3D();
        }
        return vector;
    }

    /**
     * Borrows a Vector3D from the pool and initializes it with the given values.
     * @param x the x component
     * @param y the y component
     * @param z the z component
     * @return a Vector3D instance from the pool with the specified values
     */
    public static Vector3D borrow(double x, double y, double z) {
        Vector3D vector = borrow();
        vector.set(x, y, z);
        return vector;
    }

    /**
     * Returns a Vector3D to the pool for reuse.
     * @param vector the vector to return to the pool
     */
    public static void release(Vector3D vector) {
        if (vector != null) {
            pool.offer(vector);
        }
    }

    /**
     * Returns multiple vectors to the pool.
     * @param vectors the vectors to return to the pool
     */
    public static void release(Vector3D... vectors) {
        for (Vector3D vector : vectors) {
            release(vector);
        }
    }

    /**
     * Gets the current size of the pool.
     * @return the number of available vectors in the pool
     */
    public static int getPoolSize() {
        return pool.size();
    }
}
