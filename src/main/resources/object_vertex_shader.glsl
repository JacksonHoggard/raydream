#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 aNormal;

out vec3 FragPos;
out vec3 Normal;
out vec3 viewPos;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main()
{
    FragPos = vec3(model * vec4(aPos, 1.0));
    Normal = mat3(transpose(inverse(model))) * aNormal;
    mat4 inverseView = transpose(inverse(view));
    viewPos = vec3(inverseView[0][3], inverseView[1][3], inverseView[2][3]);

    gl_Position = projection * view * vec4(FragPos, 1.0);
}