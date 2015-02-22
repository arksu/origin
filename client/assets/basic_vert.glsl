#version 120
attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord;
attribute vec4 a_color;

uniform mat4 u_MVPMatrix;
uniform mat3 u_view;

varying vec2 texCoords;
varying vec4 v_diffuse;

vec3 lightPosition = vec3(10, 10, 10);
vec4 diffuse = vec4(0.8,0.8,0.8,1);
 
void main() {

    vec3 normal = normalize( a_normal);
    vec3 lightDir = normalize(lightPosition);
    float NdotL = max(dot(normal, lightDir), 0.0);

    v_diffuse = diffuse * NdotL;
    texCoords = a_texCoord;

    gl_Position = u_MVPMatrix * a_position;
}