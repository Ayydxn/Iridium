#version 450

layout(set = 0, binding = 0) uniform UniformBufferObject {
    mat4 ViewProjectionMatrix;
} u_Camera;

layout(location = 0) in vec3 i_Position;
layout(location = 1) in vec3 i_Color;
layout(location = 2) in vec2 i_TextureCoords;

layout(location = 0) out vec3 o_Color;
layout(location = 1) out vec2 o_TextureCoords;

void main() {
    gl_Position = u_Camera.ViewProjectionMatrix * vec4(i_Position, 1.0);

    o_Color = i_Color;
    o_TextureCoords = i_TextureCoords;
}