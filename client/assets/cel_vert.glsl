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

varying vec2 v_texCoord0;

void main() {
    v_texCoord0 = a_texCoord0;
    gl_Position = a_position;
}
