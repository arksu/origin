//layout (location = 0)
out vec4 outColor;
//layout (location = 1) out vec4 outColor2;

uniform sampler2D u_texture;
uniform vec4 u_ambient;
uniform vec3 u_skyColor;
uniform sampler2D u_shadowMap;

in vec2 texCoords;
in float visibility;
in vec3 normal;

in float NdotL;

const float numShades = 16.0;

const int pcfCount = 1;
const float totalTexels = (pcfCount * 2.0 + 1.0) * (pcfCount * 2.0 + 1.0);
const float shadowMapSize = 2048.0;
const float texelSize = 1.0 / shadowMapSize;

void main() {
	float intensity = max(NdotL, 0.45);

    outColor = intensity * texture(u_texture, texCoords);
//	outColor = vec4(normal,1);
//	outColor = mix(vec4(u_skyColor, 1.0), outColor, visibility);

//	outColor = vec4(1);
//    outColor2 = vec4(0);
}
