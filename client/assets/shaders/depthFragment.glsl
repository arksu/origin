#define varying in
#define texture2D texture
#define gl_FragColor fragColor
out vec4 fragColor;

#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

#if defined(diffuseTextureFlag) && defined(blendedFlag)
#define blendedTextureFlag
varying MED vec2 v_texCoords0;
uniform sampler2D u_diffuseTexture;
uniform float u_alphaTest;
#endif

varying HIGH float v_depth;

vec4 pack_depth(const in float depth){
    const HIGH vec4 bit_shift =
        vec4(256.0*256.0*256.0, 256.0*256.0, 256.0, 1.0);
    const HIGH vec4 bit_mask  =
        vec4(0.0, 1.0/256.0, 1.0/256.0, 1.0/256.0);
    vec4 res = fract(depth * bit_shift);
    res -= res.xxyz * bit_mask;
    return res;
}

vec4 pack_depth2(const in float depth){
    const HIGH vec4 bit_shift =
        vec4(256.0, 256.0, 256.0, 1.0);
    const HIGH vec4 bit_mask  =
        vec4(0.0, 1.0/256.0, 1.0/256.0, 1.0/256.0);
    vec4 res = fract(depth * bit_shift);
    res = res.xxyz * bit_mask;
    return res;
}
void main(){
//	HIGH float depth = v_depth;
//	const HIGH vec4 bias = vec4(1.0 / 255.0, 1.0 / 255.0, 1.0 / 255.0, 0.0);
//	HIGH vec4 color = vec4(depth, fract(depth * 255.0), fract(depth * 65025.0), fract(depth * 160581375.0));
//	gl_FragColor = color - (color.yzww * bias);

    gl_FragColor = pack_depth(v_depth);
}