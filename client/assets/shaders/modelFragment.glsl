layout (location = 0) out vec4 fragColor;
layout (location = 1) out vec4 fragColor2;

#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

#if defined(specularTextureFlag) || defined(specularColorFlag)
#define specularFlag
#endif

#ifdef normalFlag
in vec3 v_normal;
#endif //normalFlag

#if defined(colorFlag)
in vec4 v_color;
#endif

#ifdef blendedFlag
in float v_opacity;
#ifdef alphaTestFlag
in float v_alphaTest;
#endif //alphaTestFlag
#endif //blendedFlag

#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
#define textureFlag
#endif

#ifdef diffuseTextureFlag
in MED vec2 v_diffuseUV;
#endif

#ifdef specularTextureFlag
in MED vec2 v_specularUV;
#endif

#ifdef diffuseColorFlag
uniform vec4 u_diffuseColor;
#endif

#ifdef diffuseTextureFlag
uniform sampler2D u_diffuseTexture;
#endif

#ifdef specularColorFlag
uniform vec4 u_specularColor;
#endif

#ifdef specularTextureFlag
uniform sampler2D u_specularTexture;
#endif

#ifdef normalTextureFlag
uniform sampler2D u_normalTexture;
#endif

#ifdef lightingFlag
in vec3 v_lightDiffuse;

#if	defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
#define ambientFlag
#endif //ambientFlag

#ifdef specularFlag
in vec3 v_lightSpecular;
#endif //specularFlag

#if defined(ambientFlag) && defined(separateAmbientFlag)
in vec3 v_ambientLight;
#endif //separateAmbientFlag

#endif //lightingFlag

#ifdef fogFlag
uniform vec4 u_fogColor;
in float v_fog;
#endif // fogFlag

// arksu
#if numDirectionalLights > 0
struct DirectionalLight
{
	vec3 color;
	vec3 direction;
};
uniform DirectionalLight u_dirLights[numDirectionalLights];
#endif // numDirectionalLights

uniform vec4 u_cameraPosition;
uniform vec3 u_skyColor;
uniform int u_selected;
in float visibility;
in float NdotL;
in float NdotL2;

in vec4 world_pos;
in vec3 world_normal;

const float numShades = 9.0;
const int pcfCount = 1;
const float totalTexels = (pcfCount * 2.0 + 1.0) * (pcfCount * 2.0 + 1.0);
uniform sampler2D u_shadowMap;
in vec4 shadowCoords;

// outline
in float v_depth;

const float shadowMapSize = 2048.0;
const float texelSize = 1.0 / shadowMapSize;

float toonify(in float intensity) {
    if (intensity > 0.7)
        return 1.0;
    else if (intensity > 0.5)
        return 0.8;
    else if (intensity > 0.25)
        return 0.4;
    else
        return 0.2;
}

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

//	vec3 L = normalize( u_dirLights[0].direction - world_pos.xyz);
//    vec3 V = normalize( u_cameraPosition.xyz - world_pos.xyz);
//    vec3 H = normalize(L + V );

	//Black color if dot product is smaller than 0.3
	//else keep the same colors
//	float edgeDetection = (dot(V, normalize(world_normal.xyz)) > 0.3) ? 1 : 0;

	float intensity = max(NdotL2, 0.45);
	float shadeIntensity = ceil(intensity * numShades)/numShades;

	#if defined(normalFlag)
		vec3 normal = v_normal;
	#endif // normalFlag

	#if defined(diffuseTextureFlag) && defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = texture(u_diffuseTexture, v_diffuseUV) * u_diffuseColor * v_color;
	#elif defined(diffuseTextureFlag) && defined(diffuseColorFlag)
		vec4 diffuse = texture(u_diffuseTexture, v_diffuseUV) * u_diffuseColor;
	#elif defined(diffuseTextureFlag) && defined(colorFlag)
		vec4 diffuse = texture(u_diffuseTexture, v_diffuseUV) * v_color;
	#elif defined(diffuseTextureFlag)
		vec4 diffuse = texture(u_diffuseTexture, v_diffuseUV);
	#elif defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = u_diffuseColor * v_color;
	#elif defined(diffuseColorFlag)
		vec4 diffuse = u_diffuseColor;
	#elif defined(colorFlag)
		vec4 diffuse = v_color;
	#else
		vec4 diffuse = vec4(1.0);
	#endif

		fragColor.rgb = diffuse.rgb;

	#if (!defined(lightingFlag))
		fragColor.rgb = diffuse.rgb;
	#elif (!defined(specularFlag))
		#if defined(ambientFlag) && defined(separateAmbientFlag)
			fragColor.rgb = (diffuse.rgb * (v_ambientLight + v_lightDiffuse));
		#else
			fragColor.rgb = (diffuse.rgb * v_lightDiffuse);
		#endif
	#else
		#if defined(specularTextureFlag) && defined(specularColorFlag)
			vec3 specular = texture(u_specularTexture, v_specularUV).rgb * u_specularColor.rgb * v_lightSpecular;
		#elif defined(specularTextureFlag)
			vec3 specular = texture(u_specularTexture, v_specularUV).rgb * v_lightSpecular;
		#elif defined(specularColorFlag)
			vec3 specular = u_specularColor.rgb * v_lightSpecular;
		#else
			vec3 specular = v_lightSpecular;
		#endif

		#if defined(ambientFlag) && defined(separateAmbientFlag)
				fragColor.rgb = (diffuse.rgb * (v_lightDiffuse + v_ambientLight)) + specular;
		#else
//				fragColor.rgb = (diffuse.rgb * (v_lightDiffuse + vec3(shadeIntensity))) + specular;
//				fragColor.rgb = (diffuse.rgb * ( vec3(0.8)));// + specular;
				fragColor.rgb = diffuse.rgb;
		#endif
	#endif //lightingFlag


	#ifdef blendedFlag
		fragColor.a = diffuse.a * v_opacity;
		#ifdef alphaTestFlag
			if (fragColor.a <= v_alphaTest)
				discard;
		#endif
	#else
		fragColor.a = 1.0;
	#endif

//	float cel_factor = toonify(max(fragColor.r, max(fragColor.g, fragColor.b)));

//	fragColor.xyz = fragColor.xyz * shadeIntensity;
//	fragColor.xyz = fragColor.xyz * cel_factor;

	// shadows
	if (shadowCoords.w > 0) {
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

		fragColor.xyz = fragColor.xyz * lightFactor;
//		fragColor.xyz = vec3(1,1,1) * lightFactor;
	}

	fragColor.xyz = fragColor.xyz * shadeIntensity;
	fragColor = mix(vec4(u_skyColor, 1.0), fragColor, visibility);

//	if (u_selected == 1) {
    	fragColor2 = pack_depth(v_depth);
//    	fragColor2.r = max(fragColor2.r, 0.5);
//	} else {
//    	fragColor2 = pack_depth(v_depth);
//    }
}
