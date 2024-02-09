#version 450

layout (binding = 2) uniform sampler2D DiffuseSampler;

layout (location = 0) in vec2 texCoord;
layout (location = 1) in vec2 oneTexel;

layout (binding = 3) uniform UBO {
    vec2 InSize;
    float Resolution;
    float Saturation;
    float MosaicSize;
};

out vec4 fragColor;

void main() {
    vec2 mosaicInSize = InSize / MosaicSize;
    vec2 fractPix = fract(texCoord * mosaicInSize) / mosaicInSize;

    vec4 baseTexel = texture(DiffuseSampler, texCoord - fractPix);

    vec3 fractTexel = baseTexel.rgb - fract(baseTexel.rgb * Resolution) / Resolution;
    float luma = dot(fractTexel, vec3(0.3, 0.59, 0.11));
    vec3 chroma = (fractTexel - luma) * Saturation;
    baseTexel.rgb = luma + chroma;
    baseTexel.a = 1.0;

    fragColor = baseTexel;
}
