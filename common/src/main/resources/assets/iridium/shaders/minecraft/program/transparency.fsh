#version 450

#define NUM_LAYERS 6

layout(binding = 1) uniform sampler2D DiffuseSampler;
layout(binding = 2) uniform sampler2D DiffuseDepthSampler;
layout(binding = 3) uniform sampler2D TranslucentSampler;
layout(binding = 4) uniform sampler2D TranslucentDepthSampler;
layout(binding = 5) uniform sampler2D ItemEntitySampler;
layout(binding = 6) uniform sampler2D ItemEntityDepthSampler;
layout(binding = 7) uniform sampler2D ParticlesSampler;
layout(binding = 8) uniform sampler2D ParticlesDepthSampler;
layout(binding = 9) uniform sampler2D WeatherSampler;
layout(binding = 10) uniform sampler2D WeatherDepthSampler;
layout(binding = 11) uniform sampler2D CloudsSampler;
layout(binding = 12) uniform sampler2D CloudsDepthSampler;

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

vec4 color_layers[NUM_LAYERS];
float depth_layers[NUM_LAYERS];
int active_layers = 0;

void try_insert(vec4 color, float depth) {
    if (color.a == 0.0) {
        return;
    }

    color_layers[active_layers] = color;
    depth_layers[active_layers] = depth;

    int jj = active_layers++;
    int ii = jj - 1;
    while (jj > 0 && depth_layers[jj] > depth_layers[ii]) {
        float depthTemp = depth_layers[ii];
        depth_layers[ii] = depth_layers[jj];
        depth_layers[jj] = depthTemp;

        vec4 colorTemp = color_layers[ii];
        color_layers[ii] = color_layers[jj];
        color_layers[jj] = colorTemp;

        jj = ii--;
    }
}

vec3 blend(vec3 dst, vec4 src) {
    return (dst * (1.0 - src.a)) + src.rgb;
}

void main() {
    color_layers[0] = vec4(texture(DiffuseSampler, texCoord).rgb, 1.0);
    depth_layers[0] = texture(DiffuseDepthSampler, texCoord).r;
    active_layers = 1;

    try_insert(texture(TranslucentSampler, texCoord), texture(TranslucentDepthSampler, texCoord).r);
    try_insert(texture(ItemEntitySampler, texCoord), texture(ItemEntityDepthSampler, texCoord).r);
    try_insert(texture(ParticlesSampler, texCoord), texture(ParticlesDepthSampler, texCoord).r);
    try_insert(texture(WeatherSampler, texCoord), texture(WeatherDepthSampler, texCoord).r);
    try_insert(texture(CloudsSampler, texCoord), texture(CloudsDepthSampler, texCoord).r);

    vec3 texelAccum = color_layers[0].rgb;
    for (int ii = 1; ii < active_layers; ++ii) {
        texelAccum = blend(texelAccum, color_layers[ii]);
    }

    fragColor = vec4(texelAccum.rgb, 1.0);
}
