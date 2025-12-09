#version 450

#include <fog.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;
layout(location = 3) in ivec2 UV2;

layout(location = 0) out float vertexDistance;
layout(location = 1) out vec2 texCoord0;
layout(location = 2) out vec4 vertexColor;

layout(set = 0, binding = 0) uniform UniformBufferObject {
    mat4 ModelViewMat;
    mat4 ProjMat;
    int FogShape;
    vec4 ColorModulator;
    float FogStart;
    float FogEnd;
    vec4 FogColor;
};

layout(binding = 1) uniform sampler2D Sampler2;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexDistance = fog_distance(Position, FogShape);
    texCoord0 = UV0;
    vertexColor = Color * texelFetch(Sampler2, UV2 / 16, 0);
}
