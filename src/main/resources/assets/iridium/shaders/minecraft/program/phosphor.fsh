#version 450

layout(binding = 1) uniform sampler2D DiffuseSampler;
layout(binding = 2) uniform sampler2D PrevSampler;

layout(binding = 3) uniform UBO {
    vec2 InSize;
    vec3 Phosphor;
};

layout(location = 0) in vec2 texCoord;
layout(location = 1) in vec2 oneTexel;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 CurrTexel = texture(DiffuseSampler, texCoord);
    vec4 PrevTexel = texture(PrevSampler, texCoord);

    fragColor = vec4(max(PrevTexel.rgb * Phosphor, CurrTexel.rgb), 1.0);
}
