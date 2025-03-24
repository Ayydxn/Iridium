#version 450

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 texCoord2;

layout(location = 0) out vec4 fragColor;

layout(binding = 2) uniform sampler2D Sampler2;

layout(binding = 1) uniform UniformBufferObject
{
    vec4 ColorModulator;
};

void main() {
    vec4 color = texture(Sampler2, texCoord2) * vertexColor;
    fragColor = color * ColorModulator;
}
