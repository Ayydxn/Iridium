#version 450

layout(binding = 1) uniform sampler2D DiffuseSampler;

layout(binding = 2) uniform UBO {
    vec2 InSize;
};

layout(location = 0) in vec2 texCoord;
layout(location = 1) in vec2 oneTexel;

layout(location = 0) out vec4 fragColor;

void main() {
    vec3 Texel0 = texture(DiffuseSampler, texCoord).rgb;
    vec3 Texel1 = texture(DiffuseSampler, texCoord + vec2(oneTexel.x, 0.0)).rgb;
    vec3 Texel2 = texture(DiffuseSampler, texCoord + vec2(0.0, oneTexel.y)).rgb;
    vec3 Texel3 = texture(DiffuseSampler, texCoord + oneTexel).rgb;

    fragColor = vec4((Texel0 + Texel1 + Texel2 + Texel3) * 0.25, 1.0);
}
