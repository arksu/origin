
out vec4 out_colour;

uniform sampler2D u_texture;//will use this next week

in vec2 texCoords;

void main(void){
	out_colour = texture(u_texture, texCoords);

//	float d= gl_FragCoord.z;
//	out_colour = vec4(d, d, d, 1.0);

}