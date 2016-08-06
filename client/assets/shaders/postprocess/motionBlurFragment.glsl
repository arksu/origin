out vec4 out_Color;

#define SIZE 10

in vec2 textureCoords[SIZE+SIZE+1];

uniform sampler2D u_texture;
uniform int u_offset;

const int max = SIZE + SIZE +1;
void main(void) {

	out_Color = vec4(0.0);

	int w = SIZE - u_offset;
	int c = 0;
	for (int i = 0+w; i < max-w; i++) {
		out_Color = out_Color + texture(u_texture, textureCoords[i]);
		c++;
	}

	out_Color = out_Color / c;
}