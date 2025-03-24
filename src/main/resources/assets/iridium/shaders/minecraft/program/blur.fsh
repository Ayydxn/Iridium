#version 450

layout(binding = 1) uniform sampler2D DiffuseSampler;

layout(binding = 2) uniform UBO {
    vec2 InSize;
    vec2 BlurDir;
    float Radius;
};

layout(location = 0) in vec2 texCoord;
layout(location = 1) in vec2 oneTexel;

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 blurred = vec4(0.0);
    float totalStrength = 0.0;
    float totalAlpha = 0.0;
    float totalSamples = 0.0;
    for(float r = -Radius; r <= Radius; r += 1.0) {
        vec4 sampleValue = texture(DiffuseSampler, texCoord + oneTexel * r * BlurDir);

		// Accumulate average alpha
        totalAlpha = totalAlpha + sampleValue.a;
        totalSamples = totalSamples + 1.0;

		// Accumulate smoothed blur
        float strength = 1.0 - abs(r / Radius);
        totalStrength = totalStrength + strength;
        blurred = blurred + sampleValue;
    }
    fragColor = vec4(blurred.rgb / (Radius * 2.0 + 1.0), totalAlpha);
}
