in vec2 a_position;
in vec2 a_texCoord0;

out vec2 textureCoords;

void main(void){

	gl_Position = vec4(a_position, 0.0, 1.0);
	textureCoords = a_texCoord0;

}