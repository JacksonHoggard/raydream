#version 330 core

#define MAX_LIGHTS 100
#define PI 3.14159265358979323846

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
uniform float opacity;
uniform sampler2D tex;
uniform bool hasTexture;
uniform bool isSelected;

float sqr(float x) {
    return x*x;
}

float schlick_fresnel(float u) {
    float m = clamp(1-u, 0, 1);
    float m2 = m*m;
    return m2*m2*m;
}

float gtr1(float NdotH, float a) {
    if(a >= 1) return 1 / PI;
    float a2 = a*a;
    float t = 1 + (a2 - 1) * sqr(NdotH);
    return (a2 - 1) / (PI * log(a2) * t);
}

float gtr2(float NdotH, float a) {
    float a2 = a*a;
    float t = 1 + (a2 - 1) * sqr(NdotH);
    return a2 / (PI * t * t);
}

float gtr2_aniso(float NdotH, float HdotX, float HdotY, float ax, float ay) {
    return 1 / (PI * ax * ay * sqr(sqr(HdotX/ax) + sqr(HdotY/ay) + sqr(NdotH)));
}

float smithG_GGX(float NdotV, float alphaG) {
    float a = alphaG * alphaG;
    float b = NdotV * NdotV;
    return 1 / (NdotV + sqrt(a + b - a * b));
}

float smithG_GGX_aniso(float NdotV, float VdotX, float VdotY, float ax, float ay) {
    return 1 / (NdotV + sqrt(sqr(VdotX*ax) + sqr(VdotY*ay) + sqr(NdotV)));
}

vec3 mon2lin(vec3 x) {
    return vec3(pow(x[0], 2.2), pow(x[1], 2.2), pow(x[2], 2.2));
}

vec3 brdf(vec3 objColor, vec3 L, vec3 V, vec3 N, vec3 X, vec3 Y) {
    float NdotL = dot(N, L);
    float NdotV = dot(N, V);
    if(NdotL < 0 || NdotV < 0) return vec3(0);

    vec3 H = normalize(L + V);
    float NdotH = dot(N, H);
    float LdotH = dot(L, H);

    vec3 Cdlin = mon2lin(objColor);
    float Cdlum = 0.3 * Cdlin.r + 0.6 * Cdlin.g + 0.1 * Cdlin.b;

    vec3 Ctint = Cdlum > 0 ? Cdlin / Cdlum : vec3(1, 1, 1);
    vec3 Cspec0 = mix(material.specular * 0.08 * mix(vec3(1, 1, 1), Ctint, material.specularTint), Cdlin, material.metallic);
    vec3 Csheen = mix(vec3(1, 1, 1), Ctint, material.sheenTint);

    float FL = schlick_fresnel(NdotL);
    float FV = schlick_fresnel(NdotV);
    float Fd90 = 0.5 + 2.0 * LdotH * LdotH * material.roughness;
    float Fd = mix(1.0, Fd90, FL) * mix(1.0, Fd90, FV);

    float Fss90 = LdotH * LdotH * material.roughness;
    float Fss = mix(1.0, Fss90, FL) * mix(1.0, Fss90, FV);
    float ss = 1.25 * (Fss * (1.0 / (NdotL + NdotV) - 0.5) + 0.5);

    float aspect = sqrt(1-material.anisotropic * 0.9);
    float ax = max(0.001, sqr(material.roughness) / aspect);
    float ay = max(0.001, sqr(material.roughness) * aspect);
    float Ds = gtr2_aniso(NdotH, dot(H, X), dot(H, Y), ax, ay);
    float FH = schlick_fresnel(LdotH);
    vec3 Fs = mix(Cspec0, vec3(1, 1, 1), FH);
    float Gs = smithG_GGX_aniso(NdotL, dot(L, X), dot(L, Y), ax, ay);
    Gs *= smithG_GGX_aniso(NdotV, dot(V, X), dot(V, Y), ax, ay);
    
    vec3 Fsheen = FH * material.sheen * Csheen;

    float Dr = gtr1(NdotH, mix(0.1, 0.001, material.clearcoatGloss));
    float Fr = mix(0.04, 1.0, FH);
    float Gr = smithG_GGX(NdotL, 0.25) * smithG_GGX(NdotV, 0.25);

    return ((1/PI) * mix(Fd, ss, material.subsurface)*Cdlin + Fsheen)
            * (1-material.metallic)
            + Gs * Fs * Ds + 0.25 * material.clearcoat * Gr * Fr * Dr;
}

void main()
{
    if(isSelected) {
        FragColor = vec4(1, 0.301, 0, 1);
        return;
    }
    vec3 objColor = material.albedo;
    if(hasTexture) {
        objColor = vec3(texture(tex, TexCoord));
    }
    // vectors
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 X = normalize(cross(vec3(0, 1, 0), norm));
    vec3 Y = cross(norm, X);

    // ambient
    vec3 ambient = ambientCoefficient * (objColor * ambientLight.color);

    vec3 result = ambient;
    for(int i = 0; i < MAX_LIGHTS; i++) {
        // vectors
        vec3 lightDir = normalize(lights[i].position - FragPos);
        vec3 reflectDir = reflect(-lightDir, norm);
        float dotNvLd = dot(norm, lightDir);

        vec3 brdf_result = vec3(0);
        if(dotNvLd > 0.0) {
            brdf_result = brdf(objColor, lightDir, viewDir, norm, X, Y);
        }

        float lightDist = length(lights[i].position - FragPos);
        result += brdf_result;
    }

    FragColor = vec4(result, opacity);
}