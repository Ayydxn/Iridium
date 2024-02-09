#version 450

layout(binding = 1) uniform sampler2D DiffuseSampler;
layout(binding = 2) uniform sampler2D OutlineSampler;

layout(location = 0) in vec2 texCoord;
layout(location = 1) in vec2 oneTexel;

layout(location = 0) out vec4 fragColor;

void main(){
    vec4 diffuseTexel = texture(DiffuseSampler, texCoord);
    vec4 outlineTexel = texture(OutlineSampler, texCoord);
    fragColor = vec4(diffuseTexel.rgb + diffuseTexel.rgb * outlineTexel.rgb * vec3(0.75), 1.0);
}
