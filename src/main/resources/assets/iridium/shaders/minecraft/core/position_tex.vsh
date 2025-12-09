#version 450

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;

layout(location = 0) out vec2 texCoord0;

layout(set = 0, binding = 0) uniform UniformBufferObject {
    uniform mat4 ModelViewMat;
    uniform mat4 ProjMat;
    uniform vec4 ColorModulator;
};

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
}
