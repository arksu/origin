in vec4 a_position;
in vec3 a_normal;
in vec2 a_texCoord;
in vec4 a_color;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform mat3 u_viewTrans;
uniform vec4 u_cameraPosition;
uniform vec3 u_cameraDirection;

out vec4 v_diffuse;
out float visibility;

out float NdotL;


vec3 lightPosition = vec3(10, 10, 10);
vec4 diffuse = vec4(2,2,2,1);

uniform float u_density;
uniform float u_gradient;

out vec4 clipSpace;

void main() {
	vec4 worldPosition = u_worldTrans * a_position;
    vec3 normal = normalize( a_normal);
    vec3 lightDir = normalize(lightPosition);
    NdotL = max(dot(normal, lightDir), 0.0);

//    v_diffuse = u_ambient * NdotL;
    v_diffuse = diffuse * NdotL;

//    texCoords = a_texCoord;
//	texCoords = vec2(a_texCoord.x/2.0 + 0.5, a_texCoord.y/2.0 + 0.5);

//    surfaceNormal = (u_worldTrans * vec4(normal, 0.0)).xyz;
//    toLightVector = lightPosition - worldPosition.xyz;

    float distance = length(u_cameraPosition.xyz - worldPosition.xyz);
    visibility = exp(-pow((distance * u_density), u_gradient));

	clipSpace = u_projViewTrans * a_position;
    gl_Position = clipSpace;
}
