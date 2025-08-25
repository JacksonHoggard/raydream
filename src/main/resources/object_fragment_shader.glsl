#version 330 core

#define MAX_LIGHTS 100
#define PI 3.14159265358979323846
#define GAMMA 2.2
#define INV_GAMMA 0.45454545

out vec4 FragColor;

struct Material {
    vec3 albedo;
    float subsurface;
    float metallic;
    vec3 specular;
    float specularTint;
    float roughness;
    float anisotropic;
    float sheen;
    float sheenTint;
    float clearcoat;
    float clearcoatGloss;
    float indexOfRefraction;
};

struct Light {
    vec3 position;
    float brightness;
    vec3 color;
};

in vec3 viewPos;
in vec3 FragPos;
in vec3 Normal;
in vec2 TexCoord;

uniform Material material;
uniform Light ambientLight;
uniform float ambientCoefficient;
uniform Light lights[MAX_LIGHTS];
uniform int numActiveLights;
uniform float opacity;
uniform sampler2D tex;
uniform bool hasTexture;
uniform bool isSelected;

float sqr(float x) {
    return x * x;
}

// Optimized Schlick fresnel with single pow operation
float schlick_fresnel(float u) {
    float m = clamp(1.0 - u, 0.0, 1.0);
    float m2 = m * m;
    return m2 * m2 * m; // m^5
}

// Optimized GTR1 with early return
float gtr1(float NdotH, float a) {
    if (a >= 1.0) return 1.0 / PI;
    float a2 = a * a;
    float t = 1.0 + (a2 - 1.0) * sqr(NdotH);
    return (a2 - 1.0) / (PI * log(a2) * t);
}

// Optimized GTR2
float gtr2(float NdotH, float a) {
    float a2 = a * a;
    float t = 1.0 + (a2 - 1.0) * sqr(NdotH);
    return a2 / (PI * t * t);
}

// Optimized anisotropic GTR2
float gtr2_aniso(float NdotH, float HdotX, float HdotY, float ax, float ay) {
    float term = sqr(HdotX / ax) + sqr(HdotY / ay) + sqr(NdotH);
    return 1.0 / (PI * ax * ay * sqr(term));
}

// Optimized Smith G masking function
float smithG_GGX(float NdotV, float alphaG) {
    float a = alphaG * alphaG;
    float b = NdotV * NdotV;
    return 1.0 / (NdotV + sqrt(a + b - a * b));
}

// Optimized anisotropic Smith G
float smithG_GGX_aniso(float NdotV, float VdotX, float VdotY, float ax, float ay) {
    return 1.0 / (NdotV + sqrt(sqr(VdotX * ax) + sqr(VdotY * ay) + sqr(NdotV)));
}

// Fast gamma correction using approximation
vec3 mon2lin(vec3 x) {
    return pow(x, vec3(GAMMA));
}

vec3 brdf(vec3 objColor, vec3 L, vec3 V, vec3 N, vec3 X, vec3 Y) {
    float NdotL = dot(N, L);
    float NdotV = dot(N, V);
    
    // Early exit for invalid geometry
    if (NdotL <= 0.0 || NdotV <= 0.0) return vec3(0.0);

    vec3 H = normalize(L + V);
    float NdotH = dot(N, H);
    float LdotH = dot(L, H);

    // Precompute common material values
    vec3 Cdlin = mon2lin(objColor);
    float Cdlum = 0.3 * Cdlin.r + 0.6 * Cdlin.g + 0.1 * Cdlin.b;
    vec3 Ctint = (Cdlum > 0.0) ? Cdlin / Cdlum : vec3(1.0);
    vec3 Cspec0 = mix(material.specular * 0.08 * mix(vec3(1.0), Ctint, material.specularTint), Cdlin, material.metallic);
    vec3 Csheen = mix(vec3(1.0), Ctint, material.sheenTint);

    // Precompute fresnel terms
    float FL = schlick_fresnel(NdotL);
    float FV = schlick_fresnel(NdotV);
    float FH = schlick_fresnel(LdotH);

    // Diffuse component (simplified)
    float Fd90 = 0.5 + 2.0 * LdotH * LdotH * material.roughness;
    float Fd = mix(1.0, Fd90, FL) * mix(1.0, Fd90, FV);

    // Subsurface scattering (simplified)
    float Fss90 = LdotH * LdotH * material.roughness;
    float Fss = mix(1.0, Fss90, FL) * mix(1.0, Fss90, FV);
    float ss = 1.25 * (Fss * (1.0 / (NdotL + NdotV) - 0.5) + 0.5);

    // Specular component with anisotropy
    float aspect = sqrt(1.0 - material.anisotropic * 0.9);
    float roughness2 = sqr(material.roughness);
    float ax = max(0.001, roughness2 / aspect);
    float ay = max(0.001, roughness2 * aspect);
    
    float Ds = gtr2_aniso(NdotH, dot(H, X), dot(H, Y), ax, ay);
    vec3 Fs = mix(Cspec0, vec3(1.0), FH);
    float Gs = smithG_GGX_aniso(NdotL, dot(L, X), dot(L, Y), ax, ay) *
               smithG_GGX_aniso(NdotV, dot(V, X), dot(V, Y), ax, ay);
    
    // Sheen component
    vec3 Fsheen = FH * material.sheen * Csheen;

    // Clearcoat component (only if needed)
    vec3 clearcoat = vec3(0.0);
    if (material.clearcoat > 0.0) {
        float Dr = gtr1(NdotH, mix(0.1, 0.001, material.clearcoatGloss));
        float Fr = mix(0.04, 1.0, FH);
        float Gr = smithG_GGX(NdotL, 0.25) * smithG_GGX(NdotV, 0.25);
        clearcoat = vec3(0.25 * material.clearcoat * Gr * Fr * Dr);
    }

    // Combine all components
    vec3 diffuse = (1.0 / PI) * mix(Fd, ss, material.subsurface) * Cdlin + Fsheen;
    vec3 specular = Gs * Fs * Ds;
    
    return diffuse * (1.0 - material.metallic) + specular + clearcoat;
}

void main()
{
    // Early exit for selected objects
    if (isSelected) {
        FragColor = vec4(1.0, 0.301, 0.0, 1.0);
        return;
    }
    
    // Get object color
    vec3 objColor = material.albedo;
    if (hasTexture) {
        objColor = texture(tex, TexCoord).rgb;
    }
    
    // Precompute common vectors
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(viewPos - FragPos);
    
    // Calculate tangent and bitangent more robustly
    vec3 X, Y;
    
    // Choose an arbitrary vector that's not parallel to the normal
    vec3 up = abs(norm.y) < 0.999 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
    
    // Create tangent space
    X = normalize(cross(up, norm));
    Y = cross(norm, X);

    // Ambient lighting
    vec3 ambient = ambientCoefficient * (objColor * ambientLight.color);
    vec3 result = ambient;
    
    // Limit light loop to actual number of active lights
    int lightCount = min(numActiveLights, MAX_LIGHTS);
    
    for (int i = 0; i < lightCount; i++) {
        // Skip lights with zero brightness
        if (lights[i].brightness <= 0.0) continue;
        
        vec3 lightDir = normalize(lights[i].position - FragPos);
        float dotNL = dot(norm, lightDir);
        
        // Skip lights behind the surface
        if (dotNL <= 0.0) continue;
        
        // Calculate BRDF
        vec3 brdfResult = brdf(objColor, lightDir, viewDir, norm, X, Y);
        
        // Apply light attenuation (distance-based)
        float lightDist = length(lights[i].position - FragPos);
        float attenuation = lights[i].brightness / (1.0 + 0.1 * lightDist + 0.01 * lightDist * lightDist);
        
        result += brdfResult * lights[i].color * attenuation;
    }

    FragColor = vec4(result, opacity);
}