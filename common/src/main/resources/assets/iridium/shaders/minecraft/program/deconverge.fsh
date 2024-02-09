#version 450

layout (binding = 1) uniform sampler2D DiffuseSampler;

layout (binding = 2) uniform UBO {
    vec2 InSize;
    vec3 ConvergeX;
    vec3 ConvergeY;
    vec3 RadialConvergeX;
    vec3 RadialConvergeY;
};

layout(location = 0) in vec2 texCoord;
layout(location = 1) in vec2 oneTexel;

layout(location = 0) out vec4 fragColor;

void main() {
    vec3 CoordX = texCoord.x * RadialConvergeX;
    vec3 CoordY = texCoord.y * RadialConvergeY;

    CoordX += ConvergeX * oneTexel.x - (RadialConvergeX - 1.0) * 0.5;
    CoordY += ConvergeY * oneTexel.y - (RadialConvergeY - 1.0) * 0.5;

    float RedValue   = texture(DiffuseSampler, vec2(CoordX.x, CoordY.x)).r;
    float GreenValue = texture(DiffuseSampler, vec2(CoordX.y, CoordY.y)).g;
    float BlueValue  = texture(DiffuseSampler, vec2(CoordX.z, CoordY.z)).b;
    float AlphaValue  = texture(DiffuseSampler, texCoord).a;

    fragColor = vec4(RedValue, GreenValue, BlueValue, 1.0);
}
