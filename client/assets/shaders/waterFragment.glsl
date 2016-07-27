out vec4 outColor;

uniform sampler2D u_reflectionTexture;
uniform sampler2D u_refractionTexture;
uniform vec4 u_ambient;
uniform vec3 u_skyColor;

in vec4 v_diffuse;
in float visibility;

in vec4 clipSpace;

void main() {
	vec2 ndc = (clipSpace.xy/clipSpace.w)/2.0 + 0.5;
	vec2 refractTexCoords = vec2(ndc.x, ndc.y);
	vec2 reflectTexCoords = vec2(ndc.x, 1-ndc.y);


    vec4 reflectColor =  texture(u_reflectionTexture, reflectTexCoords);
    vec4 refractColor =  texture(u_refractionTexture, refractTexCoords);

    outColor = mix(reflectColor, refractColor, 0.5);


//	gl_FragColor.xyz = gl_FragColor.xyz * shadeIntensity;
	outColor = mix(vec4(u_skyColor, 1.0), outColor, visibility);


}
