#version 120

uniform sampler2D u_texture;
uniform vec4 u_ambient;
uniform vec3 u_skyColor;

varying vec2 texCoords;
varying vec4 v_diffuse;
varying float visibility;

void main() {
    gl_FragColor = u_ambient * v_diffuse * texture2D(u_texture, texCoords);
//    gl_FragColor = texture2D(u_texture, texCoords);


	gl_FragColor = mix(vec4(u_skyColor, 1.0), gl_FragColor, visibility);
}
