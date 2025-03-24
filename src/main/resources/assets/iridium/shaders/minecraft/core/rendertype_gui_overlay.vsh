#version 450

layout (location = 0) in vec3 Position;
layout (location = 1) in vec4 Color;

layout (location = 0) out vec4 vertexColor;

layout (binding = 0) uniform UniformBufferObject {
    mat4 MVP;
};


void main() {
    gl_Position = MVP * vec4(Position, 1.0);

    vertexColor = Color;
}
