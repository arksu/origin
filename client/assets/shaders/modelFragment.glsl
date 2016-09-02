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
varying vec3 v_normal;
#endif //normalFlag

#if defined(colorFlag)
varying vec4 v_color;
#endif

#ifdef blendedFlag
varying float v_opacity;
#ifdef alphaTestFlag
varying float v_alphaTest;
#endif //alphaTestFlag
#endif //blendedFlag

#if defined(diffuseTextureFlag) || defined(specularTextureFlag)
#define textureFlag
#endif

#ifdef diffuseTextureFlag
varying MED vec2 v_diffuseUV;
#endif

#ifdef specularTextureFlag
varying MED vec2 v_specularUV;
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
varying vec3 v_lightDiffuse;

#if	defined(ambientLightFlag) || defined(ambientCubemapFlag) || defined(sphericalHarmonicsFlag)
#define ambientFlag
#endif //ambientFlag

#ifdef specularFlag
varying vec3 v_lightSpecular;
#endif //specularFlag

#if defined(ambientFlag) && defined(separateAmbientFlag)
varying vec3 v_ambientLight;
#endif //separateAmbientFlag

#endif //lightingFlag

#ifdef fogFlag
uniform vec4 u_fogColor;
varying float v_fog;
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
varying float visibility;
varying float NdotL;
varying float NdotL2;

in vec4 world_pos;
in vec3 world_normal;

const float numShades = 6.0;
const int pcfCount = 1;
const float totalTexels = (pcfCount * 2.0 + 1.0) * (pcfCount * 2.0 + 1.0);
uniform sampler2D u_shadowMap;
in vec4 shadowCoords;

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

void main() {

//	vec3 L = normalize( u_dirLights[0].direction - world_pos.xyz);
    vec3 V = normalize( u_cameraPosition.xyz - world_pos.xyz);
//    vec3 H = normalize(L + V );

	//Black color if dot product is smaller than 0.3
	//else keep the same colors
	float edgeDetection = (dot(V, normalize(world_normal.xyz)) > 0.3) ? 1 : 0;

	#if defined(normalFlag)
		vec3 normal = v_normal;
	#endif // normalFlag

	#if defined(diffuseTextureFlag) && defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor * v_color;
	#elif defined(diffuseTextureFlag) && defined(diffuseColorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * u_diffuseColor;
	#elif defined(diffuseTextureFlag) && defined(colorFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV) * v_color;
	#elif defined(diffuseTextureFlag)
		vec4 diffuse = texture2D(u_diffuseTexture, v_diffuseUV);
	#elif defined(diffuseColorFlag) && defined(colorFlag)
		vec4 diffuse = u_diffuseColor * v_color;
	#elif defined(diffuseColorFlag)
		vec4 diffuse = u_diffuseColor;
	#elif defined(colorFlag)
		vec4 diffuse = v_color;
	#else
		vec4 diffuse = vec4(1.0);
	#endif

		gl_FragColor.rgb = diffuse.rgb;

	#if (!defined(lightingFlag))
		gl_FragColor.rgb = diffuse.rgb;
	#elif (!defined(specularFlag))
		#if defined(ambientFlag) && defined(separateAmbientFlag)
			gl_FragColor.rgb = (diffuse.rgb * (v_ambientLight + v_lightDiffuse));
		#else
			gl_FragColor.rgb = (diffuse.rgb * v_lightDiffuse);
		#endif
	#else
		#if defined(specularTextureFlag) && defined(specularColorFlag)
			vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb * u_specularColor.rgb * v_lightSpecular;
		#elif defined(specularTextureFlag)
			vec3 specular = texture2D(u_specularTexture, v_specularUV).rgb * v_lightSpecular;
		#elif defined(specularColorFlag)
			vec3 specular = u_specularColor.rgb * v_lightSpecular;
		#else
			vec3 specular = v_lightSpecular;
		#endif

		#if defined(ambientFlag) && defined(separateAmbientFlag)
				gl_FragColor.rgb = (diffuse.rgb * (v_lightDiffuse + v_ambientLight)) + specular;
		#else
				gl_FragColor.rgb = (diffuse.rgb * v_lightDiffuse) + specular;
		#endif
	#endif //lightingFlag


	#ifdef blendedFlag
		gl_FragColor.a = diffuse.a * v_opacity;
		#ifdef alphaTestFlag
			if (gl_FragColor.a <= v_alphaTest)
				discard;
		#endif
	#else
		gl_FragColor.a = 1.0;
	#endif

	float intensity = max(NdotL2, 0.25);
	float shadeIntensity = ceil(intensity * numShades)/numShades;
	float cel_factor = toonify(max(gl_FragColor.r, max(gl_FragColor.g, gl_FragColor.b)));

//	gl_FragColor.xyz = gl_FragColor.xyz * shadeIntensity * edgeDetection;
//	gl_FragColor.xyz = gl_FragColor.xyz * cel_factor * edgeDetection;

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
	lightFactor = lightFactor * 0.5 + 0.5;

		gl_FragColor.xyz = gl_FragColor.xyz * lightFactor;
	}

	gl_FragColor = mix(vec4(u_skyColor, 1.0), gl_FragColor, visibility);
}
