out vec4 out_Color;
#define SIZE 5

in vec2 textureCoords[SIZE+SIZE+1];

uniform sampler2D u_texture;

void main(void){
//	out_Color = texture(u_texture, textureCoords);

	out_Color = vec4(0.0);
	out_Color += texture(u_texture, textureCoords[0]) * 0.0093;
    out_Color += texture(u_texture, textureCoords[1]) * 0.028002;
    out_Color += texture(u_texture, textureCoords[2]) * 0.065984;
    out_Color += texture(u_texture, textureCoords[3]) * 0.121703;
    out_Color += texture(u_texture, textureCoords[4]) * 0.175713;
    out_Color += texture(u_texture, textureCoords[5]) * 0.198596;
    out_Color += texture(u_texture, textureCoords[6]) * 0.175713;
    out_Color += texture(u_texture, textureCoords[7]) * 0.121703;
    out_Color += texture(u_texture, textureCoords[8]) * 0.065984;
    out_Color += texture(u_texture, textureCoords[9]) * 0.028002;
    out_Color += texture(u_texture, textureCoords[10]) * 0.0093;

}