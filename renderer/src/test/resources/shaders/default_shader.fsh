#version 450

layout(location = 0) out vec4 fragColor;

layout(location = 0) in vec4 o_OutColor;

void main() {
    fragColor = o_OutColor;
}