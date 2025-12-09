#version 450

#include <fog.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;

layout(location = 0) out float vertexDistance;
layout(location = 1) out vec2 texCoord0;

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

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexDistance = fog_distance(Position, FogShape);
    texCoord0 = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
}
