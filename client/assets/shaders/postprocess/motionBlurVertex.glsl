in vec2 a_position;
in vec2 a_texCoord0;

uniform vec2 u_size;
uniform float targetWidth;

#define SIZE 10

out vec2 textureCoords[SIZE+SIZE+1];

void main(void){

	gl_Position = vec4(a_position, 0.0, 1.0);

	float pixelSize = 1.0 / u_size.x;

	for (int i = -SIZE; i <= SIZE; i++) {
//		textureCoords[i+SIZE] = a_texCoord0 + vec2(pixelSize * i, 0.0);
		textureCoords[i+SIZE] = a_texCoord0 + vec2(0.0, pixelSize * i);
	}
}