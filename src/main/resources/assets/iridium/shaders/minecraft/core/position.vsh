#version 450

#include <fog.glsl>

layout(location = 0) in vec3 Position;

layout(location = 0) out float vertexDistance;

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
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexDistance = fog_distance(Position, FogShape);
}
