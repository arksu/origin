#version 140
#define varying in
#define texture2D texture
#define gl_FragColor fragColor
out vec4 fragColor;

uniform sampler2D u_texture;
uniform vec4 u_ambient;
uniform vec3 u_skyColor;

varying vec2 texCoords;
varying vec4 v_diffuse;
varying float visibility;

varying float NdotL;

const float numShades = 7.0;

void main() {
	gl_FragColor = vec4(0, 0, 1, 1);
//    gl_FragColor = u_ambient * v_diffuse * texture2D(u_texture, texCoords);

//	float intensity = max(NdotL, 0.0);
//	float shadeIntensity = ceil(intensity * numShades)/numShades;

//	gl_FragColor.xyz = gl_FragColor.xyz * shadeIntensity;
	gl_FragColor = mix(vec4(u_skyColor, 1.0), gl_FragColor, visibility);


}
