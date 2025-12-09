#version 150

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

layout(binding = 0) uniform sampler2D InSampler;

void main() {
    fragColor = texture(InSampler, texCoord);
}
