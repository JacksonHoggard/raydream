#version 330 core

#define MAX_LIGHTS 100

out vec4 FragColor;

struct Material {
    vec3 color;
    float ambient;
    float diffuse;
    float specular;
    float specularExponent;
    float metalness;
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
uniform Light lights[MAX_LIGHTS];
uniform float opacity;
uniform sampler2D tex;
uniform bool hasTexture;

void main()
{
    vec3 objColor = material.color;
    if(hasTexture) {
        objColor = vec3(texture(tex, TexCoord));
    }
    // vectors
    vec3 norm = normalize(Normal);
    vec3 viewDir = normalize(viewPos - FragPos);

    // ambient
    vec3 ambient = material.ambient * (objColor * ambientLight.color);

    vec3 result = ambient;
    for(int i = 0; i < MAX_LIGHTS; i++) {
        // vectors
        vec3 lightDir = normalize(lights[i].position - FragPos);
        vec3 reflectDir = reflect(-lightDir, norm);
        float dotNvLd = dot(norm, lightDir);

        vec3 diffuse = vec3(0);
        vec3 specular = vec3(0);
        vec3 specColor = (material.metalness * objColor) + ((1.0 - material.metalness) * vec3(1.0, 1.0, 1.0));
        if(dotNvLd > 0.0) {
            //diffuse
            float diff = min(dotNvLd, 1.0);
            diffuse = diff * material.diffuse * lights[i].color * objColor;

            // specular
            float spec = pow(clamp(max(dot(viewDir, reflectDir), 0.0), 0.0, 1.0), material.specularExponent);
            specular = material.specular * specColor * lights[i].color * spec;
        }

        float lightDist = length(lights[i].position - FragPos);
        result += (diffuse + specular) * (lights[i].brightness / lightDist);
    }

    FragColor = vec4(result, opacity);
}