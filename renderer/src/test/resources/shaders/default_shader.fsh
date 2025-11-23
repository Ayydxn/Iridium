#version 450

layout(location = 0) in vec3 o_Color;
layout(location = 1) in vec2 o_TextureCoords;

layout(location = 0) out vec4 fragColor;

layout(push_constant) uniform PushConstants
{
    vec3 SelectedColor;
} u_FrameData;

layout(binding = 1) uniform sampler2D u_Texture;

void main() {
    fragColor = texture(u_Texture, o_TextureCoords);
}