#version 330 core

out vec4 FragColor;

struct Material {
    vec3 color;
    float ambient;
    float diffuse;
    float specular;
    float specularExponent;
    float indexOfRefraction;
    float k;
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

uniform Material material;
uniform Light ambientLight;
uniform Light light;
uniform float opacity;

void main()
{
    // vectors
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(light.position - FragPos);
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);

    // ambient
    vec3 ambient = material.ambient * material.color * ambientLight.color;

    vec3 result = ambient;
    float dotNvLd = dot(norm, lightDir);
    if(dotNvLd > 0.0) {
        // Diffuse
        float diff = min(dotNvLd, 1.0);
        vec3 diffuse = diff * material.diffuse * light.color * material.color;

        // Specular
        vec3 specColor = (material.metalness * material.color) + ((1.0 - material.metalness) * vec3(1.0, 1.0, 1.0));
        float spec = pow(clamp(max(dot(viewDir, reflectDir), 0.0), 0.0, 1.0), material.specularExponent);
        vec3 specular = material.specular * specColor * light.color * spec;

        float lightDist = length(light.position - FragPos);
        result = (ambient + diffuse + specular) * (light.brightness / lightDist);
    }

    FragColor = vec4(result, opacity);
}