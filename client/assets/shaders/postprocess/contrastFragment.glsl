in vec2 textureCoords;

out vec4 out_Color;

uniform sampler2D u_texture;

const float contrast = 0.3;

void main(void) {

	out_Color = texture(u_texture, textureCoords);
	out_Color.rgb = (out_Color.rgb - 0.5) * (1.0 + contrast) + 0.5;
}