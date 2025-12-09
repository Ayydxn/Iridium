#version 450

#include <projection.glsl>

layout(location = 0) in vec3 Position;

layout(location = 0) out vec4 texProj0;

layout(set = 0, binding = 0) uniform UniformBufferObject {
    uniform mat4 ModelViewMat;
    uniform mat4 ProjMat;
    uniform float GameTime;
    uniform int EndPortalLayers;
};

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texProj0 = projection_from_position(gl_Position);
}
