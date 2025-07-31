#version 450

layout(binding = 0) uniform UniformBufferObject {
    mat4 ModelMatrix;
    mat4 ViewMatrix;
    mat4 ProjectionMatrix;
} u_Camera;

layout(location = 0) in vec3 i_Position;
layout(location = 1) in vec3 i_Color;

layout(location = 0) out vec3 o_Color;

void main() {
    gl_Position = u_Camera.ModelMatrix * u_Camera.ViewMatrix * u_Camera.ProjectionMatrix * vec4(i_Position, 1.0);

    o_Color = i_Color;
}