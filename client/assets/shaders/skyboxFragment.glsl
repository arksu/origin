layout (location = 0) out vec4 fragColor;
layout (location = 1) out vec4 fragColor2;

uniform samplerCube u_texture1;
uniform samplerCube u_texture2;
uniform float u_blendValue;

uniform vec3 u_skyColor;

in vec3 texCoords;

const float lowerLimit = 0.0;
const float upperLimit = 20.0;

void main() {
	vec4 color1 = texture(u_texture1, texCoords);
	vec4 color2 = texture(u_texture2, texCoords);
	vec4 finalColor = mix(color1, color2, u_blendValue);

	float factor = (texCoords.y - lowerLimit) / (upperLimit - lowerLimit);
	factor = clamp(factor, 0.0, 1.0);
	fragColor = mix(vec4(u_skyColor, 1.0), finalColor, factor);

	fragColor2 = vec4(0);
}