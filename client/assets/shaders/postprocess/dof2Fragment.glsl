in vec2 textureCoords;

out vec4 out_Color;

uniform sampler2D u_texture;
uniform sampler2D u_textureDepth;
uniform vec2 u_size;


float LinearizeDepth(vec2 uv)
{
  float n = 1.0; // camera z near
  float f = 1000.0; // camera z far
  float z = texture(u_textureDepth, uv).r;
  return (2.0 * n) / (f + n - z * (f - n));
}

void main()
{
  vec2 uv = textureCoords.xy;
  //vec4 sceneTexel = texture2D(sceneSampler, uv);
  float d;
  if (uv.x < 0.5) // left part
    d = LinearizeDepth(uv) * 2;
//    out_Color = texture(u_texture, uv);
//    d = 1.0;
  else // right part
    d = texture(u_textureDepth, uv).r;
//    out_Color = texture(u_textureDepth, uv);

  out_Color = vec4(d, d, d, 1.0);
}