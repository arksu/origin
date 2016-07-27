out vec4 outColor;

uniform sampler2D u_texture;
uniform vec4 u_ambient;
uniform vec3 u_skyColor;

in vec2 texCoords;
in vec4 v_diffuse;
in float visibility;

in float NdotL;

const float numShades = 7.0;

void main() {
    outColor = u_ambient * v_diffuse * texture(u_texture, texCoords);
//    gl_FragColor = texture2D(u_texture, texCoords);

	float intensity = max(NdotL, 0.0);
	float shadeIntensity = ceil(intensity * numShades)/numShades;

	outColor.xyz = outColor.xyz * shadeIntensity;
	outColor = mix(vec4(u_skyColor, 1.0), outColor, visibility);

}
