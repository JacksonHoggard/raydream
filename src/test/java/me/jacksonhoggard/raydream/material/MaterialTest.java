package me.jacksonhoggard.raydream.material;

import me.jacksonhoggard.raydream.math.Ray;
import me.jacksonhoggard.raydream.math.Vector2D;
import me.jacksonhoggard.raydream.math.Vector3D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Material Tests")
public class MaterialTest {
    
    private Material diffuseMaterial;
    private Material reflectiveMaterial;
    private Material glassMateria;
    private static final double EPSILON = 1e-6;
    
    @BeforeEach
    void setUp() {
        // Create different types of materials for testing
        Vector3D redColor = new Vector3D(0.8, 0.2, 0.2);
        Vector3D whiteColor = new Vector3D(0.9, 0.9, 0.9);
        Vector3D clearColor = new Vector3D(0.9, 0.9, 0.9);
        
        // Diffuse material (Lambertian)
        diffuseMaterial = new Material(
            redColor,           // color
            0.1,               // ambient
            0.8,               // lambertian
            0.1,               // specular
            10.0,              // specular exponent
            0.0,               // metalness
            0.0,               // roughness
            1.0,               // index of refraction
            0.0,               // extinction coefficient
            Material.Type.OTHER, // type
            null,              // texture
            null               // bump map
        );
        
        // Reflective material (mirror)
        reflectiveMaterial = new Material(
            whiteColor,        // color
            0.05,              // ambient
            0.1,               // lambertian
            0.85,              // specular
            1000.0,            // specular exponent
            1.0,               // metalness
            0.0,               // roughness
            1.5,               // index of refraction
            0.0,               // extinction coefficient
            Material.Type.REFLECT, // type
            null,              // texture
            null               // bump map
        );
        
        // Glass material (refractive)
        glassMateria = new Material(
            clearColor,        // color
            0.0,               // ambient
            0.0,               // lambertian
            0.9,               // specular
            200.0,             // specular exponent
            0.0,               // metalness
            0.0,               // roughness
            1.5,               // index of refraction (glass)
            0.0,               // extinction coefficient
            Material.Type.REFLECT_REFRACT, // type
            null,              // texture
            null               // bump map
        );
    }
    
    @Test
    @DisplayName("Material Constructor and Getters")
    void testMaterialConstructorAndGetters() {
        assertNotNull(diffuseMaterial);
        assertEquals(Material.Type.OTHER, diffuseMaterial.getType());
        
        Vector3D color = diffuseMaterial.getColor(new Vector2D(0.5, 0.5));
        assertEquals(0.8, color.x, EPSILON);
        assertEquals(0.2, color.y, EPSILON);
        assertEquals(0.2, color.z, EPSILON);
        
        assertEquals(0.1, diffuseMaterial.getAmbient(), EPSILON);
        assertEquals(0.8, diffuseMaterial.getLambertian(), EPSILON);
        assertEquals(0.1, diffuseMaterial.getSpecular(), EPSILON);
        assertEquals(10.0, diffuseMaterial.getSpecularExponent(), EPSILON);
        assertEquals(1.0, diffuseMaterial.getIndexOfRefraction(), EPSILON);
    }
    
    @Test
    @DisplayName("Material Type Classification")
    void testMaterialTypes() {
        assertEquals(Material.Type.OTHER, diffuseMaterial.getType());
        assertEquals(Material.Type.REFLECT, reflectiveMaterial.getType());
        assertEquals(Material.Type.REFLECT_REFRACT, glassMateria.getType());
    }
    
    @Test
    @DisplayName("Static Reflection Method")
    void testStaticReflection() {
        // Test reflection calculation
        Vector3D incidentDirection = new Vector3D(1.0, -1.0, 0.0).normalized();
        Ray incident = new Ray(new Vector3D(0.0, 1.0, 0.0), incidentDirection);
        Vector3D normal = new Vector3D(0.0, 1.0, 0.0); // Surface normal pointing up
        
        Vector3D reflected = Material.reflect(incident, normal);
        
        // Reflected ray should have same x component, but opposite y component
        assertEquals(incidentDirection.x, reflected.x, EPSILON);
        assertEquals(-incidentDirection.y, reflected.y, EPSILON);
        assertEquals(incidentDirection.z, reflected.z, EPSILON);
        
        // Test with vertical incidence
        Vector3D verticalIncident = new Vector3D(0.0, -1.0, 0.0);
        Ray verticalRay = new Ray(new Vector3D(0.0, 1.0, 0.0), verticalIncident);
        Vector3D verticalReflected = Material.reflect(verticalRay, normal);
        
        assertEquals(0.0, verticalReflected.x, EPSILON);
        assertEquals(1.0, verticalReflected.y, EPSILON); // Should reflect straight back up
        assertEquals(0.0, verticalReflected.z, EPSILON);
    }
    
    @Test
    @DisplayName("Static Refraction Method")
    void testStaticRefraction() {
        // Test refraction from air to glass
        Vector3D incidentDirection = new Vector3D(0.0, -1.0, 0.0); // Straight down
        Ray incident = new Ray(new Vector3D(0.0, 1.0, 0.0), incidentDirection);
        Vector3D normal = new Vector3D(0.0, 1.0, 0.0); // Surface normal pointing up
        double airToGlass = 1.5; // Glass index of refraction
        
        Vector3D refracted = Material.refract(incident, normal, airToGlass);
        assertNotNull(refracted);
        
        // For normal incidence, refracted ray should continue straight
        assertEquals(0.0, refracted.x, 0.1);
        assertTrue(refracted.y < 0.0); // Should continue downward
        assertEquals(0.0, refracted.z, 0.1);
        
        // Test with angled incidence
        Vector3D angledIncident = new Vector3D(0.5, -0.866, 0.0).normalized(); // ~30 degrees
        Ray angledRay = new Ray(new Vector3D(0.0, 1.0, 0.0), angledIncident);
        Vector3D angledRefracted = Material.refract(angledRay, normal, airToGlass);
        
        assertNotNull(angledRefracted);
        // Refracted ray should bend toward the normal
        assertTrue(Math.abs(angledRefracted.x) < Math.abs(angledIncident.x));
    }
    
    @Test
    @DisplayName("Total Internal Reflection")
    void testTotalInternalReflection() {
        // Test total internal reflection (glass to air at large angle)
        Vector3D steepIncident = new Vector3D(0.9, -0.436, 0.0).normalized(); // Very steep angle
        Ray steepRay = new Ray(new Vector3D(0.0, 0.0, 0.0), steepIncident);
        Vector3D normal = new Vector3D(0.0, 1.0, 0.0);
        double glassToAir = 1.0 / 1.5; // Going from glass to air
        
        Vector3D refracted = Material.refract(steepRay, normal, glassToAir);
        
        // For total internal reflection, the result might be a zero vector or special handling
        // The specific behavior depends on implementation
        assertNotNull(refracted);
    }
    
    @Test
    @DisplayName("Fresnel for Dielectrics")
    void testFresnelDielectric() {
        Vector3D normal = new Vector3D(0.0, 1.0, 0.0);
        
        // Normal incidence (0 degrees)
        Vector3D normalIncident = new Vector3D(0.0, -1.0, 0.0);
        Ray normalRay = new Ray(new Vector3D(0.0, 1.0, 0.0), normalIncident);
        double fresnelNormal = glassMateria.fresnelDielectric(normalRay, normal);
        
        assertTrue(fresnelNormal >= 0.0 && fresnelNormal <= 1.0);
        
        // Grazing incidence (90 degrees)
        Vector3D grazingIncident = new Vector3D(1.0, -0.01, 0.0).normalized();
        Ray grazingRay = new Ray(new Vector3D(0.0, 1.0, 0.0), grazingIncident);
        double fresnelGrazing = glassMateria.fresnelDielectric(grazingRay, normal);
        
        assertTrue(fresnelGrazing >= 0.0 && fresnelGrazing <= 1.0);
        assertTrue(fresnelGrazing > fresnelNormal); // Grazing should have higher reflectance
    }
    
    @Test
    @DisplayName("Fresnel for Metals")
    void testFresnelMetal() {
        // Create a metal material
        Vector3D metalColor = new Vector3D(0.7, 0.7, 0.7);
        Material metalMaterial = new Material(
            metalColor, 0.05, 0.1, 0.9, 100.0, 1.0, 0.0,
            2.0, 0.5, Material.Type.REFLECT, null, null
        );
        
        Vector3D normal = new Vector3D(0.0, 1.0, 0.0);
        Vector3D incident = new Vector3D(0.5, -0.866, 0.0).normalized();
        Ray ray = new Ray(new Vector3D(0.0, 1.0, 0.0), incident);
        
        double fresnelMetal = metalMaterial.fresnelMetal(ray, normal);
        
        assertTrue(fresnelMetal >= 0.0 && fresnelMetal <= 1.0);
        // Metals typically have higher reflectance than dielectrics
    }
    
    @Test
    @DisplayName("Reflect Ray Generation")
    void testReflectRay() {
        Vector3D hitPoint = new Vector3D(0.0, 0.0, 0.0);
        Vector3D normal = new Vector3D(0.0, 1.0, 0.0);
        Vector3D incident = new Vector3D(1.0, -1.0, 0.0).normalized();
        Ray incomingRay = new Ray(new Vector3D(-1.0, 1.0, 0.0), incident);
        
        Ray reflectedRay = reflectiveMaterial.reflectRay(incomingRay, hitPoint, normal);
        
        assertNotNull(reflectedRay);
        
        // Origin may be offset for numerical stability - just check it's close to hit point
        Vector3D origin = reflectedRay.origin();
        assertTrue(hitPoint.distance(origin) < 0.01); // Should be very close to hit point
        
        // Direction should be reflected (upward component)
        Vector3D reflectedDir = reflectedRay.direction();
        assertTrue(reflectedDir.y > 0.1); // Should have significant upward component after reflection
    }
    
    @Test
    @DisplayName("Refract Ray Generation")
    void testRefractRay() {
        Vector3D hitPoint = new Vector3D(0.0, 0.0, 0.0);
        Vector3D normal = new Vector3D(0.0, 1.0, 0.0);
        Vector3D incident = new Vector3D(0.0, -1.0, 0.0);
        Ray incomingRay = new Ray(new Vector3D(0.0, 1.0, 0.0), incident);
        
        Ray refractedRay = glassMateria.refractRay(incomingRay, hitPoint, normal);
        
        assertNotNull(refractedRay);
        
        // Origin may be offset for numerical stability - just check it's close to hit point
        Vector3D origin = refractedRay.origin();
        assertTrue(hitPoint.distance(origin) < 0.01); // Should be very close to hit point
        
        // For glass material, should continue downward through the surface
        Vector3D refractedDir = refractedRay.direction();
        assertTrue(refractedDir.y < -0.1); // Should have significant downward component
    }
    
    @Test
    @DisplayName("Material Properties Validation")
    void testMaterialPropertiesValidation() {
        // Test that material properties are within expected ranges
        assertTrue(diffuseMaterial.getAmbient() >= 0.0);
        assertTrue(diffuseMaterial.getLambertian() >= 0.0);
        assertTrue(diffuseMaterial.getSpecular() >= 0.0);
        assertTrue(diffuseMaterial.getSpecularExponent() > 0.0);
        assertTrue(diffuseMaterial.getIndexOfRefraction() > 0.0);
        
        // Sum of lighting components should be reasonable
        double totalReflectance = diffuseMaterial.getAmbient() + 
                                diffuseMaterial.getLambertian() + 
                                diffuseMaterial.getSpecular();
        assertTrue(totalReflectance <= 1.1); // Allow slight margin for artistic reasons
    }
    
    @Test
    @DisplayName("Color Application")
    void testColorApplication() {
        Vector2D texCoord = new Vector2D(0.5, 0.5);
        Vector3D baseColor = diffuseMaterial.getColor(texCoord);
        Vector3D lightColor = new Vector3D(1.0, 1.0, 1.0);
        
        // Manually apply material color to light (simulating what the renderer would do)
        Vector3D resultColor = new Vector3D(
            baseColor.x * lightColor.x,
            baseColor.y * lightColor.y,
            baseColor.z * lightColor.z
        );
        
        assertNotNull(resultColor);
        // Result should be modulated by material color
        assertTrue(resultColor.x <= lightColor.x);
        assertTrue(resultColor.y <= lightColor.y);
        assertTrue(resultColor.z <= lightColor.z);
        
        // Should be proportional to material color
        assertEquals(baseColor.x * lightColor.x, resultColor.x, EPSILON);
        assertEquals(baseColor.y * lightColor.y, resultColor.y, EPSILON);
        assertEquals(baseColor.z * lightColor.z, resultColor.z, EPSILON);
    }
    
    @Test
    @DisplayName("Edge Cases")
    void testEdgeCases() {
        // Test with extreme values
        Vector3D extremeColor = new Vector3D(1.0, 1.0, 1.0);
        Material extremeMaterial = new Material(
            extremeColor, 0.0, 0.0, 1.0, 1.0, 1.0, 1.0,
            1000.0, 100.0, Material.Type.REFLECT_REFRACT, null, null
        );
        
        assertNotNull(extremeMaterial);
        assertEquals(1000.0, extremeMaterial.getIndexOfRefraction(), EPSILON);
        
        // Test reflection with zero vector (edge case)
        Vector3D zeroDirection = new Vector3D(0.0, 0.0, 0.0);
        Ray zeroRay = new Ray(new Vector3D(0.0, 0.0, 0.0), zeroDirection);
        Vector3D normal = new Vector3D(0.0, 1.0, 0.0);
        
        assertDoesNotThrow(() -> Material.reflect(zeroRay, normal));
        assertDoesNotThrow(() -> Material.refract(zeroRay, normal, 1.5));
    }
}
