in vec4 a_position;
in vec3 a_normal;
in vec2 a_texCoord;
in vec4 a_color;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform mat3 u_viewTrans;
uniform vec4 u_cameraPosition;
uniform vec3 u_cameraDirection;
uniform vec3 u_lightPosition;

out float visibility;
out vec2 textureCoords;
out vec4 clipSpace;
out vec3 toCameraVector;
out vec3 fromLightVector;

uniform float u_density;
uniform float u_gradient;


const float tiling = 4;

void main() {
	vec4 worldPosition = u_worldTrans * a_position;

	textureCoords = vec2(a_texCoord.x, a_texCoord.y) * tiling;
//	textureCoords = vec2(a_position.x/2.0 + 0.5, a_position.y/2.0 + 0.5);// * tiling;

    float distance = length(u_cameraPosition.xyz - worldPosition.xyz);
    visibility = exp(-pow((distance * u_density), u_gradient));

	clipSpace = u_projViewTrans * a_position;
    gl_Position = clipSpace;
    toCameraVector = u_cameraPosition.xyz - worldPosition.xyz;
    fromLightVector = worldPosition.xyz - u_lightPosition;
}
