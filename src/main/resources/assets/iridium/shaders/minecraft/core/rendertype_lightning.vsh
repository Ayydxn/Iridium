#version 450

#include <fog.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;

layout(location = 0) out float vertexDistance;
layout(location = 1) out vec4 vertexColor;

layout(set = 0, binding = 0) uniform UniformBufferObject {
    uniform mat4 ModelViewMat;
    uniform mat4 ProjMat;
    uniform int FogShape;
    uniform vec4 ColorModulator;
    uniform float FogStart;
    uniform float FogEnd;
};

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexDistance = fog_distance(Position, FogShape);
    vertexColor = Color;
}
