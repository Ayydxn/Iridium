#version 450

layout(binding = 1) uniform sampler2D DiffuseSampler;

layout(binding = 2) uniform UBO {
    float InverseAmount;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

void main(){
    vec4 diffuseColor = texture(DiffuseSampler, texCoord);
    vec4 invertColor = 1.0 - diffuseColor;
    vec4 outColor = mix(diffuseColor, invertColor, InverseAmount);
    fragColor = vec4(outColor.rgb, 1.0);
}
