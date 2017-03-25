layout (location = 0) out vec4 outColor;
layout (location = 1) out vec4 fragColor2;

uniform sampler2D u_texture;
uniform sampler2D u_textureNormal;
uniform sampler2D u_textureSpecular;

uniform vec4 u_ambient;
uniform vec3 u_skyColor;
uniform sampler2D u_shadowMap;
uniform int u_selected;

//uniform vec4 u_joints[128];

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

in float v_depth;
in vec4 shadowCoords;

const float numShades = 16.0;

const int pcfCount = 1;
const float totalTexels = (pcfCount * 2.0 + 1.0) * (pcfCount * 2.0 + 1.0);
const float shadowMapSize = 2048.0;
const float texelSize = 1.0 / shadowMapSize;

vec4 pack_depth(const in float depth){
    const vec4 bit_shift =
        vec4(256.0*256.0*256.0, 256.0*256.0, 256.0, 1.0);
    const vec4 bit_mask  =
        vec4(0.0, 1.0/256.0, 1.0/256.0, 1.0/256.0);
    vec4 res = fract(depth * bit_shift);
    res -= res.xxyz * bit_mask;

	res.x = clamp(res.x / 2.0, 0, 0.5);
    if (u_selected == 1) res.x = res.x + 0.51;

    return res;
}

void main() {

	vec3 rNormal;
	vec4 rTexN;
	if (u_normalMap > 0) {
		rTexN = texture(u_textureNormal, texCoords);
		rNormal = normalize(mat3(v_tangent, v_binormal, v_normal) * (rTexN.xyz * 2.0 - 1.0)); // wyz
    } else {
    	rNormal = normalize(v_normal);
    }
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
	float shadeIntensity = ceil(intensity * numShades)/numShades;
    outColor = intensity * texture(u_texture, texCoords);
	if (outColor.a <= 0.01)
		discard;
//    outColor += rSpecular;

//	outColor = vec4(rVdotN);
//	outColor = mix(vec4(u_skyColor, 1.0), outColor, visibility);

//	outColor = vec4(w1);
//    outColor2 = vec4(0);

//	outColor = vec4(u_joints[0].x);

// shadows
	if (shadowCoords.w >= 0) {
		float totalShadowWeight = 0.0;

		for (int x = -pcfCount; x <= pcfCount; x++) {
			for (int y = -pcfCount; y <= pcfCount; y++) {
				float objectNearestLihgt = texture(u_shadowMap, shadowCoords.xy + vec2(x, y) * texelSize).r;
				if (shadowCoords.z > objectNearestLihgt + 0.005) {
					totalShadowWeight += 1.0;
				}
			}
		}
		totalShadowWeight /= totalTexels;
		float lightFactor = 1.0 - (totalShadowWeight * shadowCoords.w);
		lightFactor = lightFactor * 0.5 + 0.8;

		outColor.xyz = outColor.xyz * lightFactor;
	}
	outColor.xyz = outColor.xyz * shadeIntensity;
	outColor = mix(vec4(u_skyColor, 1.0), outColor, visibility);


	fragColor2 = pack_depth(v_depth);
}
