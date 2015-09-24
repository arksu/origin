#version 120


attribute vec4 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord;
attribute vec2 a_texCoord0;
attribute vec4 a_color;

uniform mat4 u_MVPMatrix;
uniform mat3 u_view;

varying vec2 texCoords;
varying vec4 v_diffuse;

vec3 lightPosition = vec3(10, 10, 10);
vec4 diffuse = vec4(0.8,0.8,0.8,1);

//uniform vec2 size;
varying vec2 v_texCoords0;
varying vec2 v_texCoords1;
varying vec2 v_texCoords2;
varying vec2 v_texCoords3;
varying vec2 v_texCoords4;
varying vec2 v_texCoords5;
varying vec2 v_texCoords6;
varying vec2 v_texCoords7;
varying vec2 v_texCoords8;

varying vec2 v_texCoord0;

void main() {

    vec3 normal = normalize( a_normal);
    vec3 lightDir = normalize(lightPosition);
    float NdotL = max(dot(normal, lightDir), 0.0);

    v_diffuse = diffuse * NdotL;
    texCoords = a_texCoord;
    vec2 size = vec2(5, 5);

    v_texCoords0 = a_texCoord + vec2(0.0 / size.x, -1.0 / size.y);
    v_texCoords1 = a_texCoord + vec2(-1.0 / size.x, 0.0 / size.y);
    v_texCoords2 = a_texCoord + vec2(0.0 / size.x, 0.0 / size.y);
    v_texCoords3 = a_texCoord + vec2(1.0 / size.x, 0.0 / size.y);
    v_texCoords4 = a_texCoord + vec2(0.0 / size.x, 1.0 / size.y);
    v_texCoords5 = a_texCoord + vec2(-1.0 / size.x, -1.0 / size.y);
    v_texCoords6 = a_texCoord + vec2(-1.0 / size.x, 1.0 / size.y);
    v_texCoords7 = a_texCoord + vec2(1.0 / size.x, -1.0 / size.y);
    v_texCoords8 = a_texCoord + vec2(1.0 / size.x, 1.0 / size.y);


//    gl_Position = u_MVPMatrix * a_position;
    v_texCoord0 = a_texCoord0;
    gl_Position = a_position;
}
