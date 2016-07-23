#version 120

uniform sampler2D u_texture;
uniform vec4 u_ambient;

varying vec2 texCoords;
varying vec4 v_diffuse;

void main() {
    gl_FragColor = u_ambient * v_diffuse * texture2D(u_texture, texCoords);
//    gl_FragColor = texture2D(u_texture, texCoords);
}
