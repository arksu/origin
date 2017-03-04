layout (location = 0) out vec4 fragColor;
//layout (location = 1) out vec4 fragColor2;

uniform samplerCube u_texture1;
uniform samplerCube u_texture2;
uniform float u_blendValue;

uniform vec3 u_skyColor;

in vec3 texCoords;

uniform vec3 v3InvWavelength;	// 1 / pow(wavelength, 4) for the red, green, and blue channels

uniform vec3 v3LightPosition;
uniform float g;
uniform float g2;

in float y;
in float l;
in float sunalt;

void main (void)
{
	float sun = max(1.0 - 0.02 * (0.1+sunalt) * l, 0.0) + 0.3 * pow(1.0-y,16.0) * (1.0-sunalt);

    float k = (
                (0.5+pow(sunalt,0.2)) * (1.5-y)
               + pow(sun, 5.2) * sunalt * (5.0 + 15.0*sunalt)
              );


//	float k = clamp(l * 2.99, 0, 1);
	fragColor = vec4(0.5,0.5,1,0) * sun;
	fragColor = mix(vec4(.3984,.5117,.7305,1), vec4(.7031,.4687,.1055,1), sun) * k;
}