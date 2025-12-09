#version 450

#include <fog.glsl>

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 texCoord0;

layout(location = 0) out vec4 fragColor;

layout(set = 0, binding = 0) uniform UniformBufferObject {
    uniform mat4 ModelViewMat;
    uniform mat4 ProjMat;
    uniform mat4 ProjMat;
    uniform vec4 ColorModulator;
    uniform float FogStart;
    uniform float FogEnd;
    uniform vec4 FogColor;
};

layout(binding = 1) uniform sampler2D Sampler0;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    color *= vertexColor * ColorModulator;
    float fragmentDistance = -ProjMat[3].z / ((gl_FragCoord.z) * -2.0 + 1.0 - ProjMat[2].z);
    fragColor = linear_fog(color, fragmentDistance, FogStart, FogEnd, FogColor);
}
