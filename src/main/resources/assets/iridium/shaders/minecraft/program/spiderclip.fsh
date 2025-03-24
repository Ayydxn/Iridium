#version 450

layout(binding = 1) uniform sampler2D DiffuseSampler;
layout(binding = 2) uniform sampler2D BlurSampler;

layout(binding = 3) uniform UBO {
    vec2 InSize;
    vec4 Scissor;
    vec4 Vignette;
};

layout(location = 0) in vec2 texCoord;
layout(location = 1) in vec2 scaledCoord;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 ScaledTexel = texture(DiffuseSampler, scaledCoord);
    vec4 BlurTexel = texture(BlurSampler, texCoord);
    vec4 OutTexel = ScaledTexel;

    // -- Alpha Clipping --
    if (scaledCoord.x < Scissor.x) OutTexel = BlurTexel;
    if (scaledCoord.y < Scissor.y) OutTexel = BlurTexel;
    if (scaledCoord.x > Scissor.z) OutTexel = BlurTexel;
    if (scaledCoord.y > Scissor.w) OutTexel = BlurTexel;

    clamp(scaledCoord, 0.0, 1.0);

    if (scaledCoord.x < Vignette.x) OutTexel = mix(BlurTexel, OutTexel, (Scissor.x - scaledCoord.x) / (Scissor.x - Vignette.x));
    if (scaledCoord.y < Vignette.y) OutTexel = mix(BlurTexel, OutTexel, (Scissor.y - scaledCoord.y) / (Scissor.y - Vignette.y));
    if (scaledCoord.x > Vignette.z) OutTexel = mix(BlurTexel, OutTexel, (Scissor.z - scaledCoord.x) / (Scissor.z - Vignette.z));
    if (scaledCoord.y > Vignette.w) OutTexel = mix(BlurTexel, OutTexel, (Scissor.w - scaledCoord.y) / (Scissor.w - Vignette.w));
    fragColor = vec4(OutTexel.rgb, 1.0);
}
