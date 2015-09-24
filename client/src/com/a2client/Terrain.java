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
	public ShaderProgram _shader;
	public ShaderProgram _shader2;

	private int _chunksRendered = 0;

	private Texture _tileAtlas;

	public Terrain()
	{

		_shader = new ShaderProgram(
				Gdx.files.internal("assets/basic_vert.glsl"),
				Gdx.files.internal("assets/basic_frag.glsl"));

		if (!_shader.isCompiled())
		{
			Gdx.app.log("Shader", _shader.getLog());
			Gdx.app.log("Shader V", _shader.getVertexShaderSource());
			Gdx.app.log("Shader F", _shader.getFragmentShaderSource());
		}
		_shader2 = new ShaderProgram(
				Gdx.files.internal("assets/cel_vert.glsl"),
				Gdx.files.internal("assets/cel_frag.glsl"));

		if (!_shader2.isCompiled())
		{
			Gdx.app.log("Shader", _shader2.getLog());
			Gdx.app.log("Shader V", _shader2.getVertexShaderSource());
			Gdx.app.log("Shader F", _shader2.getFragmentShaderSource());
		}

		_tileAtlas = Main.getAssetManager().get(Config.RESOURCE_DIR + "tiles_atlas.png", Texture.class);

	}

	public void Render(Camera _camera, Environment _environment)
	{
		_shader.begin();
		_shader.setUniformMatrix("u_MVPMatrix", _camera.combined);
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
