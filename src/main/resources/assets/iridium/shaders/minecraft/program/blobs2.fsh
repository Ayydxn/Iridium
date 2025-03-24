#version 450

layout(binding = 1) uniform sampler2D DiffuseSampler;

layout(binding = 2) uniform UBO {
    float Radius;
};

layout(location = 0) in vec2 texCoord;
layout(location = 1) in vec2 oneTexel;

layout(location = 0) out vec4 fragColor;

void main(){
    vec4 c  = texture(DiffuseSampler, texCoord);
    vec4 maxVal = c;
    for(float u = 0.0; u <= Radius; u += 1.0) {
        for(float v = 0.0; v <= Radius; v += 1.0) {
            float weight = (((sqrt(u * u + v * v) / (Radius)) > 1.0) ? 0.0 : 1.0);

            vec4 s0 = texture(DiffuseSampler, texCoord + vec2(-u * oneTexel.x, -v * oneTexel.y));
            vec4 s1 = texture(DiffuseSampler, texCoord + vec2( u * oneTexel.x,  v * oneTexel.y));
            vec4 s2 = texture(DiffuseSampler, texCoord + vec2(-u * oneTexel.x,  v * oneTexel.y));
            vec4 s3 = texture(DiffuseSampler, texCoord + vec2( u * oneTexel.x, -v * oneTexel.y));

            vec4 o0 = max(s0, s1);
            vec4 o1 = max(s2, s3);
            vec4 tempMax = max(o0, o1);
            maxVal = mix(maxVal, max(maxVal, tempMax), weight);
        }
    }

    fragColor = vec4(maxVal.rgb, 1.0);
}
