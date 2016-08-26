out vec4 outColor;

uniform sampler2D u_texture;
uniform vec4 u_ambient;
uniform vec3 u_skyColor;
uniform sampler2D u_shadowMap;

in vec2 texCoords;
in vec4 v_diffuse;
in float visibility;
in vec4 shadowCoords;

in float NdotL;

const float numShades = 7.0;

const int pcfCount = 1;
const float totalTexels = (pcfCount * 2.0 + 1.0) * (pcfCount * 2.0 + 1.0);

void main() {
//    outColor = u_ambient * v_diffuse * texture(u_texture, texCoords);
    outColor = v_diffuse * texture(u_texture, texCoords);
//    gl_FragColor = texture2D(u_texture, texCoords);

	float intensity = max(NdotL, 0.0);
	float shadeIntensity = ceil(intensity * numShades)/numShades;
//	outColor.xyz = outColor.xyz * shadeIntensity;

	// shadows
	if (shadowCoords.w > 0) {
		float mapSize = 2048.0;
		float texelSize = 1.0 / mapSize;
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
		lightFactor = max(lightFactor * 0.5 + 0.5, 0.5);
		outColor.xyz = outColor.xyz * lightFactor;
	}

	outColor = mix(vec4(u_skyColor, 1.0), outColor, visibility);

}
