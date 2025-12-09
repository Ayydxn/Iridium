#version 450

layout(location = 0) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

// NOTE: (Ayydxn) Maybe move ColorModulator out of this and into a push constant?
layout(set = 0, binding = 0) uniform UniformBufferObject {
    mat4 ModelViewMat;
    mat4 ProjMat;
    vec4 ColorModulator;
};

void main() {
    vec4 color = vertexColor;
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;
}
