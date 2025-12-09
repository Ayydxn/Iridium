#version 450

#include <fog.glsl>

layout(location = 0) in float vertexDistance;
layout(location = 1) in vec2 texCoord0;
layout(location = 2) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

layout(set = 0, binding = 0) uniform UniformBufferObject {
    mat4 ModelViewMat;
    mat4 ProjMat;
    int FogShape;
    vec4 ColorModulator;
    float FogStart;
    float FogEnd;
    vec4 FogColor;
};

layout(binding = 2) uniform sampler2D Sampler0;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
