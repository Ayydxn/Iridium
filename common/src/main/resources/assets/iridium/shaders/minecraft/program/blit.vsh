#version 450

layout(binding = 0) uniform UBO {
    mat4 ProjMat;
    vec2 OutSize;
};

layout(location = 0) in vec4 Position;

layout(location = 1) out vec2 texCoord;

void main(){
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    texCoord = Position.xy / OutSize;
}
