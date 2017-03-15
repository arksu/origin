in vec4 a_position;
in vec3 a_normal;
in vec4 a_binormal;
in vec2 a_texCoord0;
in vec4 a_color;

uniform mat4 u_worldTrans;
uniform float u_normalMap;

uniform mat4 u_projViewTrans;
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
out float visibility;
out vec3 normal;

out vec3 v_viewVec;
out vec3 v_normal;
out vec3 v_binormal;
out vec3 v_tangent;

out float NdotL;

const vec4 diffuse = vec4(1,1,1,1);

const float transitionDistance = 10.0;

vec3 qrot(vec4 q, vec3 v) {
	return v + 2.0 * cross(vec3(q), cross(vec3(q), v) + q.w * v);
}

void main() {
	vec4 worldPosition = u_worldTrans * a_position;

    normal = normalize(a_normal);

	v_viewVec = u_cameraPosition.xyz - worldPosition.xyz;

    mat3 MM = mat3(vec3(u_worldTrans[0]), vec3(u_worldTrans[1]), vec3(u_worldTrans[2]));
	vec3 n = a_normal;// * 2.0 - 1.0;
    if (u_normalMap > 0) {

		vec4 b = a_binormal;// * 2.0 - 1.0;

		v_tangent = MM * (cross(n, vec3(b)) * b.w);
        v_binormal = MM * vec3(b);
    }
    v_normal = MM * n;

    vec3 lightPos = u_lightPosition;
    lightPos = (vec4(lightPos, 1) * u_worldTrans).xyz;
    vec3 lightDir = normalize(lightPos);
    NdotL = max(dot(normal, lightDir), 0.0);

    float distance = length(u_cameraPosition.xyz - worldPosition.xyz);
    visibility = exp(-pow((distance * u_density), u_gradient));

    gl_Position = u_projViewTrans * u_worldTrans * a_position;



    texCoords = a_texCoord0;
}