out vec4 outColor;

uniform sampler2D u_reflectionTexture;
uniform sampler2D u_refractionTexture;
uniform sampler2D u_dudvMap;
uniform vec4 u_ambient;
uniform vec3 u_skyColor;
uniform float u_moveFactor;

in vec4 v_diffuse;
in float visibility;
in vec2 textureCoords;
in vec3 toCameraVector;

in vec4 clipSpace;

const float waveStrength = 0.012;

void main() {
	vec2 ndc = (clipSpace.xy/clipSpace.w)/2.0 + 0.5;
	vec2 refractTexCoords = vec2(ndc.x, ndc.y);
	vec2 reflectTexCoords = vec2(ndc.x, 1-ndc.y);

	vec2 distorion1 = (texture(u_dudvMap, vec2(textureCoords.x+u_moveFactor, textureCoords.y)).rg * 2.0 - 1.0) * waveStrength;
	vec2 distorion2 = (texture(u_dudvMap, vec2(-textureCoords.x+u_moveFactor, textureCoords.y+u_moveFactor)).rg * 2.0 - 1.0) * waveStrength;
	vec2 totalDistortion = distorion1 + distorion2;

	refractTexCoords += totalDistortion;
	refractTexCoords = clamp(refractTexCoords, 0.001, 0.999);

	reflectTexCoords += totalDistortion;
	reflectTexCoords.x = clamp(reflectTexCoords.x, 0.001, 0.999);
	reflectTexCoords.y = clamp(reflectTexCoords.y, 0.001, 0.999);

    vec4 reflectColor =  texture(u_reflectionTexture, reflectTexCoords);
    vec4 refractColor =  texture(u_refractionTexture, refractTexCoords);

	vec3 viewVector = normalize(toCameraVector);
	float refractiveFactor = dot(viewVector, vec3(0.0, 1.0, 0.0));
	refractiveFactor = pow(refractiveFactor, 0.6);

    outColor = mix(reflectColor, refractColor, refractiveFactor);
	outColor = mix(outColor, vec4(0.0, 0.3, 0.5, 1.0), 0.2);
	outColor = mix(vec4(u_skyColor, 1.0), outColor, visibility);

}
