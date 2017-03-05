in vec4 a_position;

uniform mat4 u_projTrans;
uniform mat4 u_viewTrans;

out vec3 texCoords;

uniform vec4 u_cameraPosition;

//======================================================================================================================
//
// Atmospheric scattering vertex shader
//
// Author: Sean O'Neil
//
// Copyright (c) 2004 Sean O'Neil
//

uniform vec3 v3LightPosition;	// The direction vector to the light source
uniform vec3 v3InvWavelength;	// 1 / pow(wavelength, 4) for the red, green, and blue channels
uniform float fCameraHeight;	// The camera's current height
uniform float fCameraHeight2;	// fCameraHeight^2
uniform float fOuterRadius;		// The outer (atmosphere) radius
uniform float fOuterRadius2;	// fOuterRadius^2
uniform float fInnerRadius;		// The inner (planetary) radius
uniform float fInnerRadius2;	// fInnerRadius^2
uniform float fKrESun;			// Kr * ESun
uniform float fKmESun;			// Km * ESun
uniform float fKr4PI;			// Kr * 4 * PI
uniform float fKm4PI;			// Km * 4 * PI
uniform float fScale;			// 1 / (fOuterRadius - fInnerRadius)
uniform float fScaleDepth;		// The scale depth (i.e. the altitude at which the atmosphere's average density is found)
uniform float fScaleOverScaleDepth;	// fScale / fScaleDepth

uniform float u_size;

const int nSamples = 4;
const float fSamples = 4.0;

out vec3 v3Direction;
out vec3 c0;
out vec3 c1;
out float v3antiray;

float scale(float fCos)
{
	float x = 1.0 - fCos;
	return fScaleDepth * exp(-0.00287 + x*(0.459 + x*(3.83 + x*(-6.80 + x*5.25))));
}

void main()
{
	vec3 cameraPosition = u_cameraPosition.xyz;
	vec3 position = a_position.xyz;

	// Get the ray from the camera to the vertex and its length (which is the far point of the ray passing through the atmosphere)
	// луч из камеры до точки на сфере
	vec3 v3Ray = position - cameraPosition;
	// длина луча из камеры до вершины
	float fFar = length(v3Ray);
	// единичный вектор (направление) на вершину из камеры
	v3Ray /= fFar;

	// Calculate the closest intersection of the ray with the outer atmosphere
	// (which is the near point of the ray passing through the atmosphere)
	float B = 2.0 * dot(cameraPosition, v3Ray);
	float C = fCameraHeight2 - fOuterRadius2;
	float fDet = max(0.0, B*B - 4.0 * C);
	float fNear = 0.5 * (-B - sqrt(fDet));


    float fStartOffset;
    vec3 v3Start;

	// Calculate the ray's starting position, then calculate its scattering offset
    // за пределами атмосферы?
        v3Start = cameraPosition;
        fFar -= fNear;
        float fStartAngle = dot(v3Ray, v3Start) / fOuterRadius;
        float fStartDepth = exp(fScaleOverScaleDepth * (fInnerRadius - fCameraHeight));

        fStartOffset = fStartDepth * scale(fStartAngle);

//        v3Start = cameraPosition;
//        float fHeight1 = length(v3Start);
//        float fDepth1 = exp(fScaleOverScaleDepth * (fInnerRadius - fCameraHeight));
//        float fStartAngle1 = dot(v3Ray, v3Start) / fHeight1;
//        fStartOffset = fDepth1*scale(fStartAngle1);


	// Initialize the scattering loop variables
	float fSampleLength = fFar / fSamples;
	float fScaledLength = fSampleLength * fScale;
	vec3 v3SampleRay = v3Ray * fSampleLength;
	vec3 v3SamplePoint = v3Start + v3SampleRay * 0.5;

	// Now loop through the sample rays
	vec3 v3FrontColor = vec3(0.0, 0.0, 0.0);
	for(int i=0; i<nSamples; i++)
	{
		float fHeight = length(v3SamplePoint);
		float fDepth = exp(fScaleOverScaleDepth * (fInnerRadius - fHeight));
		float fLightAngle = dot(v3LightPosition, v3SamplePoint) / fHeight;
		float fCameraAngle = dot(v3Ray, v3SamplePoint) / fHeight;
		float fScatter = (fStartOffset + fDepth * (scale(fLightAngle) - scale(fCameraAngle)));
		vec3 v3Attenuate = exp(-fScatter * (v3InvWavelength * fKr4PI + fKm4PI));

		v3FrontColor += v3Attenuate * (fDepth * fScaledLength);
		v3SamplePoint += v3SampleRay;
	}
	// Finally, scale the Mie and Rayleigh colors and set up the varying variables for the pixel shader
	c0 = v3FrontColor * (v3InvWavelength * fKrESun);
	c1 = v3FrontColor * fKmESun;
	v3Direction = cameraPosition - position;
	gl_Position = u_projTrans * u_viewTrans * a_position;

    texCoords = a_position.xyz;

	// хитры хак чтобы заполнить остальное небо (за пределами солнца) одним цветом
    v3antiray = 1.5 - dot(v3Ray, v3LightPosition);
    v3antiray = clamp(v3antiray, 0, 1);
}