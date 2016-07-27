#version 140
#define varying in
#define texture2D texture
#define gl_FragColor fragColor
out vec4 fragColor;

#ifdef GL_ES
#define LOWP lowp
#define MED mediump
precision lowp float;
#else
#define LOWP
#define MED
#endif

uniform sampler2D u_depthTexture;
//varying MED vec2 v_texCoords0;
//varying MED vec2 v_texCoords1;
//varying MED vec2 v_texCoords2;
//varying MED vec2 v_texCoords3;
//varying MED vec2 v_texCoords4;

varying MED vec2 v_texCoords0;
varying MED vec2 v_texCoords1;
varying MED vec2 v_texCoords2;
varying MED vec2 v_texCoords3;
varying MED vec2 v_texCoords4;
varying MED vec2 v_texCoords5;
varying MED vec2 v_texCoords6;
varying MED vec2 v_texCoords7;
varying MED vec2 v_texCoords8;

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

    float depth = abs(
      unpack_depth(texture2D(u_depthTexture, v_texCoords0))
    + unpack_depth(texture2D(u_depthTexture, v_texCoords1))
    - unpack_depth(8 * texture2D(u_depthTexture, v_texCoords2))
    + unpack_depth(texture2D(u_depthTexture, v_texCoords3))
    + unpack_depth(texture2D(u_depthTexture, v_texCoords4))
    + unpack_depth(texture2D(u_depthTexture, v_texCoords5))
    + unpack_depth(texture2D(u_depthTexture, v_texCoords6))
    + unpack_depth(texture2D(u_depthTexture, v_texCoords7))
    + unpack_depth(texture2D(u_depthTexture, v_texCoords8))
    ) ;

//    float depth =
//        abs(unpack_depth(texture2D(u_depthTexture, v_texCoords0))
//    + unpack_depth(texture2D(u_depthTexture, v_texCoords1))
//    - unpack_depth(4.0 * texture2D(u_depthTexture, v_texCoords2))
//    + unpack_depth(texture2D(u_depthTexture, v_texCoords3))
//    + unpack_depth(texture2D(u_depthTexture, v_texCoords4)));
//    if(depth > 0.0004){

    if(depth > 0.0004){
        gl_FragColor = vec4(0.0,0.0,0.0,1.0);
    }
    else{
        discard;
    }
}