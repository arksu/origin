
out vec4 out_colour;

uniform sampler2D u_diffuseTexture;

in vec2 textureCoords;

void main(void){
	float alpha = texture(u_diffuseTexture, textureCoords).a;
	if (alpha < 0.5) {
		discard;
	}
	out_colour = vec4(1.0);
}