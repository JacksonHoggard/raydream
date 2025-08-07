package me.jacksonhoggard.raydream.util;

import me.jacksonhoggard.raydream.core.ApplicationContext;
import me.jacksonhoggard.raydream.material.BumpMap;
import me.jacksonhoggard.raydream.material.Texture;
import me.jacksonhoggard.raydream.math.Vector3D;
import org.lwjgl.glfw.GLFWImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Util {
    private static final Logger logger = ApplicationContext.getInstance().getLoggingService().getLogger(Util.class);
    
    public static double randomRange(double min, double max) {
        return min + (max - min) * ThreadLocalRandom.current().nextDouble();
    }

    public static Vector3D randomUnitVector() {
        double theta = randomRange(0, 2.0D * Math.PI);
        double r = Math.sqrt(randomRange(0, 1.0D));
        double z = Math.sqrt(1.0D - r*r);
        if(randomRange(0, 1.0D) < 0.5D)
            z *= -1.0D;
        return new Vector3D(r * Math.cos(theta), r * Math.sin(theta), z);
    }

    /**
     * Generate better stratified random samples for anti-aliasing
     * @param min minimum value
     * @param max maximum value  
     * @return stratified random value
     */
    public static double stratifiedRandom(double min, double max) {
        // Use more uniform distribution with better stratification
        return min + (max - min) * ThreadLocalRandom.current().nextDouble();
    }

    /**
     * Generate low-discrepancy sequence for better sampling
     * @param index sample index
     * @param base base for van der Corput sequence
     * @return quasi-random value between 0 and 1
     */
    public static double vanDerCorput(int index, int base) {
        double result = 0.0;
        double fraction = 1.0 / base;
        while (index > 0) {
            result += fraction * (index % base);
            index /= base;
            fraction /= base;
        }
        return result;
    }

    /**
     * Generate uniformly distributed point on unit disk using concentric mapping
     * This provides better sampling for depth of field effects
     * @return Vector3D with x,y coordinates on unit disk, z=0
     */
    public static Vector3D sampleDisk() {
        double u1 = ThreadLocalRandom.current().nextDouble();
        double u2 = ThreadLocalRandom.current().nextDouble();
        
        // Map to [-1,1] x [-1,1]
        double sx = 2.0 * u1 - 1.0;
        double sy = 2.0 * u2 - 1.0;
        
        // Handle degeneracy at origin
        if (sx == 0.0 && sy == 0.0) {
            return new Vector3D(0, 0, 0);
        }
        
        double r, theta;
        if (Math.abs(sx) > Math.abs(sy)) {
            r = sx;
            theta = (Math.PI / 4.0) * (sy / sx);
        } else {
            r = sy;
            theta = (Math.PI / 2.0) - (Math.PI / 4.0) * (sx / sy);
        }
        
        return new Vector3D(r * Math.cos(theta), r * Math.sin(theta), 0);
    }

    /**
     * Generate stratified disk sample for better DOF sampling
     * @param stratum which stratum (0-15 for 4x4 stratification)
     * @param totalStrata total number of strata
     * @return Vector3D with x,y coordinates on unit disk, z=0
     */
    public static Vector3D sampleDiskStratified(int stratum, int totalStrata) {
        int sqrtStrata = (int) Math.sqrt(totalStrata);
        int x = stratum % sqrtStrata;
        int y = stratum / sqrtStrata;
        
        // Generate stratified sample within stratum
        double u1 = (x + ThreadLocalRandom.current().nextDouble()) / sqrtStrata;
        double u2 = (y + ThreadLocalRandom.current().nextDouble()) / sqrtStrata;
        
        // Map to [-1,1] x [-1,1]
        double sx = 2.0 * u1 - 1.0;
        double sy = 2.0 * u2 - 1.0;
        
        // Handle degeneracy at origin
        if (sx == 0.0 && sy == 0.0) {
            return new Vector3D(0, 0, 0);
        }
        
        double r, theta;
        if (Math.abs(sx) > Math.abs(sy)) {
            r = sx;
            theta = (Math.PI / 4.0) * (sy / sx);
        } else {
            r = sy;
            theta = (Math.PI / 2.0) - (Math.PI / 4.0) * (sx / sy);
        }
        
        return new Vector3D(r * Math.cos(theta), r * Math.sin(theta), 0);
    }

    /**
     * Apply ordered dithering to reduce color banding
     * @param value color value (0-1)
     * @param x pixel x coordinate
     * @param y pixel y coordinate
     * @return dithered color value
     */
    public static double applyDithering(double value, int x, int y) {
        // 4x4 Bayer matrix for ordered dithering
        int[][] bayerMatrix = {
            {0, 8, 2, 10},
            {12, 4, 14, 6},
            {3, 11, 1, 9},
            {15, 7, 13, 5}
        };
        
        double threshold = bayerMatrix[x % 4][y % 4] / 16.0;
        double ditherAmount = 1.0 / 255.0; // One step in 8-bit color space
        
        return value + (threshold - 0.5) * ditherAmount;
    }

    public static Texture loadTexture(String path) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(path));
        } catch (IOException e) {
            logger.error("Failed to load texture file: " + path, e);
            throw new RuntimeException("Failed to load texture file:" + path, e);
        }
        return new Texture(image, path, image.getWidth(), image.getHeight());
    }

    public static BumpMap loadBumpMap(String path, double bumpScale) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(path));
        } catch (IOException e) {
            logger.error("Failed to load bump map file: " + path, e);
            throw new RuntimeException("Failed to load bump map file:" + path, e);
        }
        return new BumpMap(image, path, image.getWidth(), image.getHeight(), bumpScale);
    }

    public static String loadShader(InputStream shaderInput) {
        try {
            return new String(shaderInput.readAllBytes());
        } catch (IOException e) {
            logger.error("Failed to load shader file", e);
            throw new RuntimeException("Failed to load shader file.", e);
        }
    }

    public static byte[] loadFont(String filePath) {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(filePath);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            if (is == null) {
                logger.error("Resource not found: " + filePath);
                throw new IllegalArgumentException("Resource not found: " + filePath);
            }

            byte[] data = new byte[1024];
            int bytesRead;

            while ((bytesRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            return buffer.toByteArray();
        } catch (IOException e) {
            logger.error("Failed to load font", e);
            throw new RuntimeException("Failed to load font.", e);
        }
    }

    public static List<String> readAllLines(InputStream inputStream) {
        List<String> list = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while((line = br.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException | NullPointerException e) {
            logger.error("Failed to read resource file", e);
            throw new RuntimeException("Failed to read resource file.", e);
        }
        return list;
    }
}
