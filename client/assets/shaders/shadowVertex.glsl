#define MAX_JOINTS 64

in vec3 a_position;
in vec2 a_texCoord0;

in vec2 a_bone0;
in vec2 a_bone1;

uniform float u_skinMode;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;

uniform vec4 u_joints[MAX_JOINTS * 2];


out vec2 textureCoords;

vec3 qrot(vec4 q, vec3 v) {
	return v + 2.0 * cross(vec3(q), cross(vec3(q), v) + q.w * v);
}

void main(void){
	vec3 worldPosition;
	vec4 qq0;

	if (u_skinMode > 0) {
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
    gl_Position = u_projViewTrans * vec4(worldPosition, 1.0);


    textureCoords = a_texCoord0;
//	gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);

}