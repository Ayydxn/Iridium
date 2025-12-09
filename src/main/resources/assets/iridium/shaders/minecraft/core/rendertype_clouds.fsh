#version 450

#include <fog.glsl>

layout(location = 0) in float vertexDistance;
layout(location = 1) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

layout(set = 0, binding = 0) uniform UniformBufferObject {
    uniform mat4 ModelViewMat;
    uniform mat4 ProjMat;
    uniform vec3 ModelOffset;
    uniform int FogShape;
    uniform vec4 ColorModulator;
    uniform float FogStart;
    uniform float FogEnd;
    uniform vec4 FogColor;
};

void main() {
    fragColor = linear_fog(vertexColor, vertexDistance, FogStart, FogEnd, FogColor);
}
