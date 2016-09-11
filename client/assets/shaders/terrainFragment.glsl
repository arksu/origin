layout (location = 0) out vec4 outColor;
layout (location = 1) out vec4 outColor2;

uniform sampler2D u_texture;
uniform vec4 u_ambient;
uniform vec3 u_skyColor;
uniform sampler2D u_shadowMap;

in vec2 texCoords;
in vec4 v_diffuse;
in float visibility;
in vec4 shadowCoords;

in float NdotL;

const float numShades = 16.0;

const int pcfCount = 1;
const float totalTexels = (pcfCount * 2.0 + 1.0) * (pcfCount * 2.0 + 1.0);
const float shadowMapSize = 2048.0;
const float texelSize = 1.0 / shadowMapSize;

in float v_depth;

vec4 pack_depth(const in float depth){
    const vec4 bit_shift =
        vec4(256.0*256.0*256.0, 256.0*256.0, 256.0, 1.0);
    const vec4 bit_mask  =
        vec4(0.0, 1.0/256.0, 1.0/256.0, 1.0/256.0);
    vec4 res = fract(depth * bit_shift);
    res -= res.xxyz * bit_mask;
    return res;
}

void main() {
	float intensity = max(NdotL, 0.25);
	float shadeIntensity = ceil(intensity * numShades) / numShades;

//    outColor = (v_diffuse) * texture(u_texture, texCoords);
    outColor = (shadeIntensity) * texture(u_texture, texCoords);
//	outColor.xyz = outColor.xyz * shadeIntensity;

	// shadows
	if (shadowCoords.w > 0) {
		float totalShadowWeight = 0.0;

		for (int x = -pcfCount; x <= pcfCount; x++) {
			for (int y = -pcfCount; y <= pcfCount; y++) {
				float objectNearestLihgt = texture(u_shadowMap, shadowCoords.xy + vec2(x, y) * texelSize).r;
				if (shadowCoords.z > objectNearestLihgt + 0.004) {
					totalShadowWeight += 1.0;
				}
			}
		}
		totalShadowWeight /= totalTexels;
		float lightFactor = 1.0 - (totalShadowWeight * shadowCoords.w);
		lightFactor = max(lightFactor , 0.5);
		outColor.xyz = outColor.xyz * lightFactor;
	}

	outColor = mix(vec4(u_skyColor, 1.0), outColor, visibility);
//    outColor2 = pack_depth(v_depth);
    outColor2 = vec4(0);
}
