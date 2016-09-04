out vec4 fragColor;

uniform sampler2D u_texture;
uniform sampler2D u_textureDepth;

//in vec2 v_texCoords0;
//in vec2 v_texCoords1;
//in vec2 v_texCoords2;
//in vec2 v_texCoords3;
//in vec2 v_texCoords4;

in vec2 v_texCoords0;
in vec2 v_texCoords1;
in vec2 v_texCoords2;
in vec2 v_texCoords3;
in vec2 v_texCoords4;
in vec2 v_texCoords5;
in vec2 v_texCoords6;
in vec2 v_texCoords7;
in vec2 v_texCoords8;

float unpack_depth(const in vec4 rgba_depth){
	vec4 pd = rgba_depth;
	if (pd.x > 0.5) pd -= 0.5;
	pd.x *= 2;
    const vec4 bit_shift =
        vec4(1.0/(256.0*256.0*256.0)
            , 1.0/(256.0*256.0)
            , 1.0/256.0
            , 1.0);
    float depth = dot(pd, bit_shift);
    return depth;
}
float unpack_depth2(const in vec4 rgba_depth){
	vec4 pd = rgba_depth;
	if (pd.x > 0.5) pd -= 0.5;
	pd.x *= 2;
	pd = pd * 4.0; // !!!!
    const vec4 bit_shift =
        vec4(1.0/(256.0*256.0*256.0)
            , 1.0/(256.0*256.0)
            , 1.0/256.0
            , 1.0);
    float depth = dot(pd, bit_shift);
    return depth;
}

void main(){
//	float depth = (texture(u_textureDepth, v_texCoords0).a
//        + texture(u_textureDepth, v_texCoords1).a
//        + texture(u_textureDepth, v_texCoords2).a
//    + texture(u_textureDepth, v_texCoords3).a
//    + texture(u_textureDepth, v_texCoords4).a
//    + texture(u_textureDepth, v_texCoords5).a
//    + texture(u_textureDepth, v_texCoords6).a
//    + texture(u_textureDepth, v_texCoords7).a
//    + texture(u_textureDepth, v_texCoords8).a) / 9.0;

//    float depth = abs(
//      unpack_depth(texture(u_textureDepth, v_texCoords0))
//    + unpack_depth(texture(u_textureDepth, v_texCoords1))
//    - unpack_depth(8 * texture(u_textureDepth, v_texCoords2))
//    + unpack_depth(texture(u_textureDepth, v_texCoords3))
//    + unpack_depth(texture(u_textureDepth, v_texCoords4))
//    + unpack_depth(texture(u_textureDepth, v_texCoords5))
//    + unpack_depth(texture(u_textureDepth, v_texCoords6))
//    + unpack_depth(texture(u_textureDepth, v_texCoords7))
//    + unpack_depth(texture(u_textureDepth, v_texCoords8))
//    ) ;

//	float d = unpack_depth(texture(u_textureDepth, v_texCoords2));
//	float visibility = exp(-pow((d * 0.01), 2.5));
//	float visibility = d / 3;//-pow(d * 0.01, 2.5);

    float depth =
        abs(unpack_depth(texture(u_textureDepth, v_texCoords0))
    + unpack_depth(texture(u_textureDepth, v_texCoords1))
    - unpack_depth2(texture(u_textureDepth, v_texCoords2))
    + unpack_depth(texture(u_textureDepth, v_texCoords3))
    + unpack_depth(texture(u_textureDepth, v_texCoords4)));

	float d1 = texture(u_textureDepth, v_texCoords2).r;
	vec4 dcolor;
	if (d1 > 0.5) {
		dcolor = vec4(1,0,0,1);
	} else {
		dcolor = vec4(0,0,0,1);
	}
    if(d1 != 0 && depth > 0.0003) {
        fragColor = mix(dcolor, texture(u_texture, v_texCoords2), 0.3);
    } else {
    	fragColor = texture(u_texture, v_texCoords2);
//        discard;
    }

//      fragColor = vec4(d1);
//      fragColor = vec4(unpack_depth(texture(u_textureDepth, v_texCoords2)));

//    fragColor = texture(u_texture, v_texCoords2);
//    fragColor = texture(u_textureDepth, v_texCoords2);
}