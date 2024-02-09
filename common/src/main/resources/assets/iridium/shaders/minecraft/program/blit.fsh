#version 450

layout (binding = 1) uniform sampler2D DiffuseSampler;

layout (binding = 2) uniform UBO {
    vec4 ColorModulate;
};

layout (location = 0) in vec2 texCoord;

layout (location = 1) out vec4 fragColor;

void main(){
    fragColor = texture(DiffuseSampler, texCoord) * ColorModulate;
}
