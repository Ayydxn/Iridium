#version 450

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 texCoord0;
layout(location = 2) in vec2 texCoord2;

layout(location = 0) out vec4 fragColor;

layout(binding = 2) uniform sampler2D Sampler0;

layout(binding = 1) uniform UniformBufferObject
{
    vec4 ColorModulator;
};

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = color * ColorModulator;
}
