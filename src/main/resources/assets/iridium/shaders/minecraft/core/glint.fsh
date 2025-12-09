#version 450

#include <fog.glsl>

layout(location = 0) in float vertexDistance;
layout(location = 1) in vec2 texCoord0;

layout(location = 0) out vec4 fragColor;

layout(set = 0, binding = 0) uniform UniformBufferObjet {
    mat4 ModelViewMat;
    mat4 ProjMat;
    mat4 TextureMat;
    int FogShape;
    vec4 ColorModulator;
    float FogStart;
    float FogEnd;
    float GlintAlpha;
};

layout(binding = 1) uniform sampler2D Sampler0;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
    float fade = linear_fog_fade(vertexDistance, FogStart, FogEnd) * GlintAlpha;
    fragColor = vec4(color.rgb * fade, color.a);
}
