#version 450

layout(binding = 0) uniform UBO {
    mat4 ProjMat;
    vec2 InSize;
    vec2 OutSize;
    vec2 ScreenSize;
};

layout(location = 0) in vec4 Position;

layout(location = 0) out vec2 texCoord;

void main(){
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    vec2 inOutRatio = OutSize / InSize;
    vec2 inScreenRatio = ScreenSize / InSize;
    texCoord = Position.xy / OutSize;
    texCoord.y = 1.0 - texCoord.y;
    texCoord.x = texCoord.x * inOutRatio.x;
    texCoord.y = texCoord.y * inOutRatio.y;
    texCoord.y -= 1.0 - inScreenRatio.y;
}
