#version 450

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 texCoord0;

layout(location = 0) out vec4 fragColor;

layout(set = 0, binding = 0) uniform UniformBufferObject {
    uniform mat4 ModelViewMat;
    uniform mat4 ProjMat;
    uniform vec4 ColorModulator;
};

layout(binding = 2) uniform sampler2D Sampler0;
layout(binding = 1) uniform sampler2D Sampler2;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    fragColor = color * ColorModulator;
}
