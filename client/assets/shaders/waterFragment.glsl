out vec4 outColor;

uniform sampler2D u_reflectionTexture;
uniform sampler2D u_refractionTexture;
uniform sampler2D u_dudvMap;
uniform sampler2D u_normalMap;
uniform vec4 u_ambient;
uniform vec3 u_skyColor;
uniform float u_moveFactor;
uniform vec3 u_lightColor;

in vec4 v_diffuse;
in float visibility;
in vec2 textureCoords;
in vec3 toCameraVector;
in vec3 fromLightVector;

in vec4 clipSpace;

const float waveStrength = 0.017;
const float shineDamper = 30.0;
const float reflectivity = 0.4;

void main() {
	vec2 ndc = (clipSpace.xy/clipSpace.w)/2.0 + 0.5;
	vec2 refractTexCoords = vec2(ndc.x, ndc.y);
	vec2 reflectTexCoords = vec2(ndc.x, 1-ndc.y);

	vec2 distortedTexCoords = texture(u_dudvMap, vec2(textureCoords.x + u_moveFactor, textureCoords.y)).rg*0.1;
	distortedTexCoords = textureCoords + vec2(distortedTexCoords.x, distortedTexCoords.y+u_moveFactor);
	vec2 totalDistortion = (texture(u_dudvMap, distortedTexCoords).rg * 2.0 - 1.0) * waveStrength;

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

	vec4 normalMapColor = texture(u_normalMap, distortedTexCoords);
	vec3 normal = vec3(normalMapColor.r * 2.0 - 1.0, normalMapColor.b, normalMapColor.g * 2.0 - 1.0);
	normal = normalize(normal);

	vec3 reflectedLight = reflect(normalize(fromLightVector), normal);
    float specular = max(dot(reflectedLight, viewVector), 0.0);
    specular = pow(specular, shineDamper);
    vec3 specularHighlights = u_lightColor * specular * reflectivity;

    outColor = mix(reflectColor, refractColor, refractiveFactor);
	outColor = mix(outColor, vec4(0.0, 0.3, 0.5, 1.0), 0.2) + vec4(specularHighlights, 0.0);
	outColor = mix(vec4(u_skyColor, 1.0), outColor, visibility);
}
