#version 450

layout(binding = 0) uniform UBO {
    mat4 ProjMat;
    vec2 InSize;
    vec2 OutSize;
    vec2 InScale;
    vec2 InOffset;
    float InRotation;
    float Time;
};

layout(location = 0) in vec4 Position;

layout(location = 0) out vec2 texCoord;
layout(location = 1) out vec2 scaledCoord;

void main(){
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    texCoord = Position.xy / OutSize;

    float Deg2Rad = 0.0174532925;
    float InRadians = InRotation * Deg2Rad;
    float Cosine = cos(InRadians);
    float Sine = sin(InRadians);
    float RotU = texCoord.x * Cosine - texCoord.y * Sine;
    float RotV = texCoord.y * Cosine + texCoord.x * Sine;
    scaledCoord = vec2(RotU, RotV) * InScale + InOffset;
}
