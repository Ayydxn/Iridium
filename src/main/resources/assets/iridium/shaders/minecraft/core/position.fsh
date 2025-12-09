#version 450

#include <fog.glsl>

layout(location = 0) in float vertexDistance;

layout(location = 0) out vec4 fragColor;

layout(set = 0, binding = 0) uniform UniformBufferObject {
    mat4 ProjMat;
    mat4 ModelViewMat;
    int FogShape;
    vec4 ColorModulator;
    float FogStart;
    float FogEnd;
    vec4 FogColor;
};

void main() {
    fragColor = linear_fog(ColorModulator, vertexDistance, FogStart, FogEnd, FogColor);
}
