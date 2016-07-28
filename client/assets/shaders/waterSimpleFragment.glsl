out vec4 outColor;

uniform sampler2D u_texture;
uniform vec4 u_ambient;
uniform vec3 u_skyColor;

in vec2 texCoords;
in vec4 v_diffuse;
in float visibility;

in float NdotL;

void main() {
    outColor = vec4(0.0, 0.3, 0.5, 1);
    outColor = u_ambient * v_diffuse * outColor;

	outColor = mix(vec4(u_skyColor, 1.0), outColor, visibility);

	outColor.a = 0.7;
}
