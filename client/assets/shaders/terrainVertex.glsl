in vec4 a_position;
in vec3 a_normal;
in vec2 a_texCoord0;
in vec4 a_color;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform mat3 u_viewTrans;
uniform vec4 u_cameraPosition;
uniform vec3 u_cameraDirection;
uniform vec4 u_ambient;
uniform vec4 u_clipPlane;
uniform vec3 u_lightPosition;
uniform mat4 u_toShadowMapSpace;
uniform float u_shadowDistance;

uniform float u_density;
uniform float u_gradient;

out vec2 texCoords;
out vec4 shadowCoords;
out float visibility;

out float NdotL;

const vec4 diffuse = vec4(1,1,1,1);

const float transitionDistance = 10.0;

void main() {
	vec4 worldPosition = u_worldTrans * a_position;

	gl_ClipDistance[0] = dot(worldPosition, u_clipPlane);

    vec3 normal = normalize(a_normal);
    vec3 lightDir = normalize(u_lightPosition);
    NdotL = max(dot(normal, lightDir), 0.0);

    float distance = length(u_cameraPosition.xyz - worldPosition.xyz);
    visibility = exp(-pow((distance * u_density), u_gradient));

    gl_Position = u_projViewTrans * a_position;

	// shadow
	if (u_shadowDistance > 0) {
		shadowCoords = u_toShadowMapSpace * worldPosition;
		distance = distance - (u_shadowDistance - transitionDistance);
		distance = distance / transitionDistance;
		shadowCoords.w = clamp(1.0 - distance, 0.0, 1.0);
    } else {
    	shadowCoords.w = -1.0;
    }

    texCoords = a_texCoord0;
}