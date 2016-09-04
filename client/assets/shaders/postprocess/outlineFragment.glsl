out vec4 fragColor;

uniform sampler2D u_texture;

in vec2 v_texCoords0;
in vec2 v_texCoords1;
in vec2 v_texCoords2;
in vec2 v_texCoords3;
in vec2 v_texCoords4;

//in vec2 v_texCoords0;
//in vec2 v_texCoords1;
//in vec2 v_texCoords2;
//in vec2 v_texCoords3;
//in vec2 v_texCoords4;
//in vec2 v_texCoords5;
//in vec2 v_texCoords6;
//in vec2 v_texCoords7;
//in vec2 v_texCoords8;

float unpack_depth(const in vec4 rgba_depth){
    const vec4 bit_shift =
        vec4(1.0/(256.0*256.0*256.0)
            , 1.0/(256.0*256.0)
            , 1.0/256.0
            , 1.0);
    float depth = dot(rgba_depth, bit_shift);
    return depth;
}

void main(){

//    float depth = abs(
//      unpack_depth(texture(u_texture, v_texCoords0))
//    + unpack_depth(texture(u_texture, v_texCoords1))
//    - unpack_depth(8 * texture(u_texture, v_texCoords2))
//    + unpack_depth(texture(u_texture, v_texCoords3))
//    + unpack_depth(texture(u_texture, v_texCoords4))
//    + unpack_depth(texture(u_texture, v_texCoords5))
//    + unpack_depth(texture(u_texture, v_texCoords6))
//    + unpack_depth(texture(u_texture, v_texCoords7))
//    + unpack_depth(texture(u_texture, v_texCoords8))
//    ) ;

    float depth =
        abs(unpack_depth(texture(u_texture, v_texCoords0))
    + unpack_depth(texture(u_texture, v_texCoords1))
    - unpack_depth(4.0 * texture(u_texture, v_texCoords2))
    + unpack_depth(texture(u_texture, v_texCoords3))
    + unpack_depth(texture(u_texture, v_texCoords4)));
//    if(depth > 0.0004) {

    if(depth > 0.0004) {
        fragColor = vec4(0.0,0.0,0.0,1.0);
    } else {
    	fragColor = vec4(1.0,1.0,1.0,0.0);
//        discard;
    }


//    fragColor = texture(u_texture, v_texCoords2);
}