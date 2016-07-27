#version 140
#define varying in
#define texture2D texture
#define gl_FragColor fragColor
out vec4 fragColor;

//precision lowp float;

uniform sampler2D u_texture;
uniform vec4 u_ambient;

varying vec2 texCoords;
varying vec4 v_diffuse;

//uniform sampler2D u_texture;
varying vec2 v_texCoords0;
varying vec2 v_texCoords1;
varying vec2 v_texCoords2;
varying vec2 v_texCoords3;
varying vec2 v_texCoords4;
varying vec2 v_texCoords5;
varying vec2 v_texCoords6;
varying vec2 v_texCoords7;
varying vec2 v_texCoords8;

varying vec2 v_texCoord0;

float toonify(in float intensity) {
    if (intensity > 0.8)
        return 1.0;
    else if (intensity > 0.5)
        return 0.8;
//    else if (intensity > 0.25)
//        return 0.3;
    else
        return 0.4;
}

void main() {
    vec4 color = texture2D(u_texture, v_texCoord0);
    float factor = toonify(max(color.r, max(color.g, color.b)));
    gl_FragColor = vec4(factor*color.rgb, color.a);
//    gl_FragColor = color;
}
