#version 450

#include <fog.glsl>

layout(location = 0) in float vertexDistance;
layout(location = 1) in vec4 vertexColor;
layout(location = 2) in vec2 texCoord0;

layout(location = 0) out vec4 fragColor;

layout(set = 0, binding = 0) uniform UniformBufferObject {
    uniform mat4 ModelViewMat;
    uniform mat4 ProjMat;
    uniform int FogShape;
    uniform vec4 ColorModulator;
    uniform float FogStart;
    uniform float FogEnd;
    uniform vec4 FogColor;
};

layout(binding = 1) uniform sampler2D Sampler0;

void main() {
    vec4 color = texture(Sampler0, clamp(texCoord0, 0.0, 1.0));
    color *= vertexColor * ColorModulator;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
