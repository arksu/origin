#define varying out
#define attribute in

attribute vec4 a_position;

uniform mat4 u_projTrans;
uniform mat4 u_viewTrans;

varying vec3 texCoords;

void main() {
	gl_Position = u_projTrans * u_viewTrans * a_position;
    texCoords = a_position.xyz;
}