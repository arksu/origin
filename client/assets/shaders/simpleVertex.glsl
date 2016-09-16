in vec3 a_position;
in vec2 a_texCoord0;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;

out vec2 textureCoords;

void main(void){
    textureCoords = a_texCoord0;
	gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
}