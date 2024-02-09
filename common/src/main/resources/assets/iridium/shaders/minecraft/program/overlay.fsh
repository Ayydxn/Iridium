#version 450

layout(binding = 1) uniform sampler2D DiffuseSampler;
layout(binding = 2) uniform sampler2D OverlaySampler;

layout(binding = 3) uniform UBO {
    vec2 InSize;
    float MosaicSize;
    vec3 RedMatrix;
    vec3 GreenMatrix;
    vec3 BlueMatrix;
};

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec2 mosaicInSize = InSize / MosaicSize;
    vec2 fractPix = fract(texCoord * mosaicInSize) / mosaicInSize;

    vec4 baseTexel = texture(DiffuseSampler, texCoord - fractPix);
    float red = dot(baseTexel.rgb, RedMatrix);
    float green = dot(baseTexel.rgb, GreenMatrix);
    float blue = dot(baseTexel.rgb, BlueMatrix);

    vec4 overlayTexel = texture(OverlaySampler, vec2(texCoord.x, 1.0 - texCoord.y));
    overlayTexel.a = 1.0;
    fragColor = mix(vec4(red, green, blue, 1.0), overlayTexel, overlayTexel.a);
}
