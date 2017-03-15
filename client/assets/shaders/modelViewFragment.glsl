//layout (location = 0)
out vec4 outColor;
//layout (location = 1) out vec4 outColor2;

uniform sampler2D u_texture;
uniform sampler2D u_textureNormal;
uniform sampler2D u_textureSpecular;

uniform vec4 u_ambient;
uniform vec3 u_skyColor;
uniform sampler2D u_shadowMap;

uniform float u_specularMap;
uniform float u_normalMap;

in vec2 texCoords;
in float visibility;
in vec3 normal;

in float NdotL;

in vec3 v_viewVec;
in vec3 v_normal;
in vec3 v_binormal;
in vec3 v_tangent;



const float numShades = 16.0;

const int pcfCount = 1;
const float totalTexels = (pcfCount * 2.0 + 1.0) * (pcfCount * 2.0 + 1.0);
const float shadowMapSize = 2048.0;
const float texelSize = 1.0 / shadowMapSize;

void main() {

    vec4 rTexN = texture(u_textureNormal, texCoords);
    vec3 rNormal = normalize(mat3(v_tangent, v_binormal, v_normal) * (rTexN.xyz * 2.0 - 1.0)); // wyz
    vec3 rViewVec = normalize(v_viewVec);
	float rVdotN = dot(rViewVec, rNormal);

	vec4 rSpecular = vec4(0.3);// uMaterial[cSpecular];
	if (u_specularMap > 0) {
		rSpecular *= texture(u_textureSpecular, texCoords);
	} else if (u_normalMap > 0) {
		rSpecular.xyz *= rTexN.x;
	}

	float intensity = max(rVdotN, 0.45);
//	float intensity = max(NdotL, 0.45);
//    outColor = intensity * texture(u_texture, texCoords);
    outColor = intensity * texture(u_texture, texCoords);

    outColor += rSpecular;

//	outColor = vec4(intensity);
//	outColor = mix(vec4(u_skyColor, 1.0), outColor, visibility);

//	outColor = vec4(1);
//    outColor2 = vec4(0);
}
