#version 450

layout(binding = 0) uniform UBO {
    mat4 ProjMat;
    vec2 OutSize;
    float SubPixelShift;
};

layout(location = 0) in vec4 Position;

layout(location = 0) out vec2 texCoord;
layout(location = 1) out vec4 posPos;

void main() {
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    texCoord = Position.xy / OutSize;
    posPos.xy = texCoord.xy;
    posPos.zw = texCoord.xy - (1.0/OutSize * vec2(0.5 + SubPixelShift));
}
