#version 120

uniform samplerCube u_texture;

varying vec3 texCoords;

void main() {
	gl_FragColor = textureCube(u_texture, texCoords);
}