#version 450

layout(binding = 1) uniform sampler2D DiffuseSampler;

layout(location = 0) out vec2 texCoord;
layout(location = 1) out vec2 oneTexel;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 center = texture(DiffuseSampler, texCoord);
    vec4 up = texture(DiffuseSampler, texCoord + vec2(0.0, -oneTexel.y));
    vec4 down = texture(DiffuseSampler, texCoord + vec2(oneTexel.x, 0.0));
    vec4 left = texture(DiffuseSampler, texCoord + vec2(-oneTexel.x, 0.0));
    vec4 right = texture(DiffuseSampler, texCoord + vec2(0.0, oneTexel.y));
    vec4 uDiff = center - up;
    vec4 dDiff = center - down;
    vec4 lDiff = center - left;
    vec4 rDiff = center - right;
    vec4 sum = uDiff + dDiff + lDiff + rDiff;
    vec3 clamped = clamp(center.rgb - sum.rgb, 0.0, 1.0);
    fragColor = vec4(clamped, 1.0);
}
