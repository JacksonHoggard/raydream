#version 330 core

out vec4 FragColor;

uniform vec3 color;
uniform bool isSelected;

void main()
{
    if(isSelected) {
        FragColor = vec4(1, 0.301, 0, 1);
        return;
    }
    FragColor = vec4(color, 1.0);
}