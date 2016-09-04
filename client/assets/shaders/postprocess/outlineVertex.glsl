uniform vec2 u_size;

in vec4 a_position;
in vec2 a_texCoord0;

out vec2 v_texCoords0;
out vec2 v_texCoords1;
out vec2 v_texCoords2;
out vec2 v_texCoords3;
out vec2 v_texCoords4;

//out vec2 v_texCoords0;
//out vec2 v_texCoords1;
//out vec2 v_texCoords2;
//out vec2 v_texCoords3;
//out vec2 v_texCoords4;
//out vec2 v_texCoords5;
//out vec2 v_texCoords6;
//out vec2 v_texCoords7;
//out vec2 v_texCoords8;

void main(){

//	v_texCoords0 = a_texCoord0 + vec2(0.0 / u_size.x, -1.0 / u_size.y);
//    v_texCoords1 = a_texCoord0 + vec2(-1.0 / u_size.x, 0.0 / u_size.y);
//    v_texCoords2 = a_texCoord0 + vec2(0.0 / u_size.x, 0.0 / u_size.y);
//    v_texCoords3 = a_texCoord0 + vec2(1.0 / u_size.x, 0.0 / u_size.y);
//    v_texCoords4 = a_texCoord0 + vec2(0.0 / u_size.x, 1.0 / u_size.y);
//    v_texCoords5 = a_texCoord0 + vec2(-1.0 / u_size.x, -1.0 / u_size.y);
//    v_texCoords6 = a_texCoord0 + vec2(-1.0 / u_size.x, 1.0 / u_size.y);
//    v_texCoords7 = a_texCoord0 + vec2(1.0 / u_size.x, -1.0 / u_size.y);
//    v_texCoords8 = a_texCoord0 + vec2(1.0 / u_size.x, 1.0 / u_size.y);
   
    v_texCoords0 = a_texCoord0 + vec2(0.0, -1.0 / u_size.y);
    v_texCoords1 = a_texCoord0 + vec2(-1.0 / u_size.x, 0.0);
    v_texCoords2 = a_texCoord0 + vec2(0.0, 0.0);
    v_texCoords3 = a_texCoord0 + vec2(1.0 / u_size.x, 0.0);
    v_texCoords4 = a_texCoord0 + vec2(0.0, 1.0 / u_size.y);

    gl_Position = a_position;
}