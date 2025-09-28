#version 450

layout(location = 0) in vec3 o_Color;

layout(location = 0) out vec4 fragColor;

layout(push_constant) uniform PushConstants
{
    vec3 SelectedColor;
} u_FrameData;

void main() {
    fragColor = vec4(u_FrameData.SelectedColor, 1.0);
}