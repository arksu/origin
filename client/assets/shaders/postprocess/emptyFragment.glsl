in vec2 textureCoords;

out vec4 out_Color;

uniform sampler2D u_texture;

void main(void) {
	out_Color = texture(u_texture, textureCoords);
}