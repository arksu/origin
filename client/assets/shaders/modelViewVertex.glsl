#define MAX_JOINTS 64

in vec3 a_position;
in vec3 a_normal;
in vec4 a_binormal;
in vec2 a_texCoord0;
in vec4 a_color;

in vec2 a_bone0;
in vec2 a_bone1;

uniform mat4 u_worldTrans;
uniform int u_normalMapFlag;
uniform int u_skinFlag;

uniform mat4 u_projViewTrans;
uniform mat3 u_viewTrans;
uniform vec4 u_cameraPosition;
uniform vec3 u_cameraDirection;
uniform vec4 u_ambient;
uniform vec4 u_clipPlane;
uniform vec3 u_lightPosition;
uniform mat4 u_toShadowMapSpace;
uniform float u_shadowDistance;

uniform vec4 u_joints[MAX_JOINTS * 2];


uniform float u_density;
uniform float u_gradient;

out vec2 texCoords;
out float visibility;
out vec3 normal;

out vec3 v_viewVec;
out vec3 v_normal;
out vec3 v_binormal;
out vec3 v_tangent;
out float v_depth;
out vec4 shadowCoords;

out float NdotL;

const vec4 diffuse = vec4(1,1,1,1);

const float transitionDistance = 10.0;

vec3 qrot(vec4 q, vec3 v) {
	return v + 2.0 * cross(vec3(q), cross(vec3(q), v) + q.w * v);
}

void main() {
	vec3 worldPosition;
	vec4 qq0;

	if (u_skinFlag > 0) {
		vec2 rWeight;
		// unpack weight
//		rWeight.x = aJoint.z * (1.0 / 255.0);
		rWeight.x = a_bone0.x;

		rWeight.y = 1.0 - rWeight.x;
//		rWeight.y = a_bone1.x;

		// joint idex
		ivec2 rJoint = ivec2(a_bone0.y * 2 , a_bone1.y * 2);

		rWeight.y *= step(0.0, dot(u_joints[rJoint.x], u_joints[rJoint.y])) * 2.0 - 1.0;

		qq0 = u_joints[rJoint.x] * rWeight.x + u_joints[rJoint.y] * rWeight.y;
		vec4 qq1 = u_joints[rJoint.x + 1] * rWeight.x + u_joints[rJoint.y + 1] * rWeight.y;

		float len = 1.0 / length(qq0);
		qq0 *= len;
		qq1 *= len;

		vec3 jpos = 2.0 * (qq0.w * vec3(qq1) - qq1.w * vec3(qq0) + cross(vec3(qq0), vec3(qq1)));

		worldPosition = vec3(u_worldTrans * vec4(qrot(qq0, a_position) + jpos, 1.0));
	} else {
		worldPosition = vec3(u_worldTrans * vec4(a_position, 1.0));
	}

    normal = normalize(a_normal);

	v_viewVec = u_cameraPosition.xyz - worldPosition.xyz;

    mat3 MM = mat3(vec3(u_worldTrans[0]), vec3(u_worldTrans[1]), vec3(u_worldTrans[2]));
	vec3 n = a_normal;// * 2.0 - 1.0;
	vec4 b = a_binormal;// * 2.0 - 1.0;

	if (u_skinFlag > 0) {
		if (u_normalMapFlag > 0) {
			v_tangent = MM * qrot(qq0, (cross(n, vec3(b)) * b.w));
			v_binormal = MM * qrot(qq0, vec3(b));
    	}
    	v_normal = MM * qrot(qq0, n);
	} else {
		if (u_normalMapFlag > 0) {
			v_tangent = MM * (cross(n, vec3(b)) * b.w);
			v_binormal = MM * vec3(b);
		}
		v_normal = MM * n;
    }

    vec3 lightPos = u_lightPosition;
//    lightPos = (vec4(lightPos, 1) * u_worldTrans).xyz;
    vec3 lightDir = normalize(lightPos);
//    NdotL = max(dot(normal, lightDir), 0.0);
    v_viewVec = lightDir;

    float distance = length(u_cameraPosition.xyz - worldPosition.xyz);
    visibility = exp(-pow((distance * u_density), u_gradient));

    gl_Position = u_projViewTrans * vec4(worldPosition, 1.0);
	gl_ClipDistance[0] = dot(worldPosition, u_clipPlane.xyz);

    texCoords = a_texCoord0;

    float z = -gl_Position.z-1;
	z /= 1900;
	v_depth = (z);

	// shadow
	if (u_shadowDistance > 0) {
		vec4 worldPosition1 = u_worldTrans * vec4(a_position, 1.0);
		shadowCoords = u_toShadowMapSpace * worldPosition1;
		distance = distance - (u_shadowDistance - transitionDistance);
		distance = distance / transitionDistance;
		shadowCoords.w = clamp(1.0 - distance, 0.0, 1.0);
	} else {
		shadowCoords.w = -1.0;
	}
}