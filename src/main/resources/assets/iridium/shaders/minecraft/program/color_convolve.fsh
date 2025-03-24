#version 450

layout(binding = 1) uniform sampler2D DiffuseSampler;

layout (binding = 2) uniform UBO {
    vec2 InSize;
    vec3 Gray;
    vec3 RedMatrix;
    vec3 GreenMatrix;
    vec3 BlueMatrix;
    vec3 Offset;
    vec3 ColorScale;
    float Saturation;
};

layout(location = 0) in vec2 texCoord;
layout(location = 1) in vec2 oneTexel;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 InTexel = texture(DiffuseSampler, texCoord);

    // Color Matrix
    float RedValue = dot(InTexel.rgb, RedMatrix);
    float GreenValue = dot(InTexel.rgb, GreenMatrix);
    float BlueValue = dot(InTexel.rgb, BlueMatrix);
    vec3 OutColor = vec3(RedValue, GreenValue, BlueValue);

    // Offset & Scale
    OutColor = (OutColor * ColorScale) + Offset;

    // Saturation
    float Luma = dot(OutColor, Gray);
    vec3 Chroma = OutColor - Luma;
    OutColor = (Chroma * Saturation) + Luma;

    fragColor = vec4(OutColor, 1.0);
}
