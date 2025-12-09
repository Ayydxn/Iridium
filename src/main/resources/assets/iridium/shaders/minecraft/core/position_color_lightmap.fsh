#version 450

layout(location = 0) in vec4 vertexColor;
layout(location = 1) in vec2 texCoord2;

layout(location = 0) out vec4 fragColor;

layout(set = 0, binding = 0) uniform UniformBufferObject {
    mat4 ModelViewMat;
    mat4 ProjMat;
    vec4 ColorModulator;
};

layout(bidning = 1) uniform sampler2D Sampler2;

void main() {
    vec4 color = texture(Sampler2, texCoord2) * vertexColor;
    fragColor = color * ColorModulator;
}
