in vec4 a_position;

uniform mat4 u_projTrans;
uniform mat4 u_viewTrans;

uniform vec3 v3LightPosition;	// The direction vector to the light source


out vec3 texCoords;

out float y;
out float sunalt;
out float l;

uniform vec4 u_cameraPosition;
uniform float u_size;

/**
* неудачная попытка шейдера для неба
**/
void main() {
	vec3 sun = v3LightPosition * u_size;
	y = a_position.y / u_size;
	float len = (length(a_position.xyz - sun) );
//	l = (len / (u_size)) * 280;
	l = len;
	l = l * 2;
//	l = v3LightPosition.y;
	sunalt = v3LightPosition.y;

	gl_Position = u_projTrans * u_viewTrans * a_position;
    texCoords = a_position.xyz;
}