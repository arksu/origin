package com.a2client;

import com.a2client.model.Grid;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Terrain
{
	public ShaderProgram _shaderBasic;
	public ShaderProgram _shaderCel;
	public ShaderProgram _shaderOutline;
	public ShaderProgram _shaderDepth;

	public ShaderProgram _shader;

	private int _chunksRendered = 0;

	private Texture _tileAtlas;

	public Terrain()
	{

		_shaderBasic = makeShader("assets/basic_vert.glsl", "assets/basic_frag.glsl");
		_shaderCel = makeShader("assets/cel_vert.glsl", "assets/cel_frag.glsl");
		_shaderOutline = makeShader("assets/outline_vert.glsl", "assets/outline_frag.glsl");
		_shaderDepth = makeShader("assets/depth_vertex.glsl", "assets/depth_frag.glsl");

		_tileAtlas = Main.getAssetManager().get(Config.RESOURCE_DIR + "tiles_atlas.png", Texture.class);

	}

	public ShaderProgram makeShader(String vert, String frag)
	{
		ShaderProgram program = new ShaderProgram(
				Gdx.files.internal(vert),
				Gdx.files.internal(frag));

		if (!program.isCompiled())
		{
			Gdx.app.log("Shader", program.getLog());
			Gdx.app.log("Shader V", program.getVertexShaderSource());
			Gdx.app.log("Shader F", program.getFragmentShaderSource());
		}
		return program;
	}

	public void Render(Camera _camera, Environment _environment)
	{
		_shader.begin();
		_shader.setUniformMatrix("u_MVPMatrix", _camera.combined);
		_shader.setUniformMatrix("u_projViewWorldTrans", _camera.combined);
//        _shader.setUniformMatrix("u_view", _camera.view);
		_shader.setUniformi("u_texture", 0);

		_tileAtlas.bind();

		_shader.setUniformf("u_ambient", ((ColorAttribute) _environment.get(ColorAttribute.AmbientLight)).color);
		_chunksRendered = 0;
		for (Grid grid : MapCache.grids)
		{
			_chunksRendered += grid.render(_shader, _camera);
		}
		_shader.end();
	}

	public int getChunksRendered()
	{
		return _chunksRendered;
	}
}
