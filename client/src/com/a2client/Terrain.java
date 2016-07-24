package com.a2client;

import com.a2client.model.Grid;
import com.a2client.render.Fog;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Terrain
{
	private static final Logger _log = LoggerFactory.getLogger(Terrain.class.getName());

	public ShaderProgram _shaderTerrain;
	public ShaderProgram _shaderCel;
	public ShaderProgram _shaderOutline;
	public ShaderProgram _shaderDepth;

	public ShaderProgram _shader;

	private int _chunksRendered = 0;

	private Texture _tileAtlas;

	public Terrain()
	{

		_shaderTerrain = makeShader("assets/terrainVertex.glsl", "assets/terrainFragment.glsl");
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
			_log.warn("Terrain shader ERROR " + program.getLog());
			_log.warn("Shader V " + program.getVertexShaderSource());
			_log.warn("Shader F " + program.getFragmentShaderSource());
		}
		return program;
	}

	public void Render(Camera camera, Environment environment)
	{
		_shader.begin();
		_shader.setUniformMatrix("u_projTrans", camera.projection);
		_shader.setUniformMatrix("u_viewTrans", camera.view);
		_shader.setUniformMatrix("u_projViewTrans", camera.combined);

		_shader.setUniformf("u_cameraPosition", camera.position.x, camera.position.y, camera.position.z,
							1.1881f / (camera.far * camera.far));
		_shader.setUniformf("u_cameraDirection", camera.direction);

		_shader.setUniformMatrix("u_worldTrans", new Matrix4().idt());

		_shader.setUniformf("u_ambient", ((ColorAttribute) environment.get(ColorAttribute.AmbientLight)).color);
//		_shader.setUniformi("u_texture", 0);

		_shader.setUniformf("u_skyColor", Fog.skyColor.r, Fog.skyColor.g, Fog.skyColor.b);
		_shader.setUniformf("u_density", Fog.enabled ? Fog.density : 0f);
		_shader.setUniformf("u_gradient", Fog.gradient);

		_tileAtlas.bind();

		_chunksRendered = 0;
		for (Grid grid : MapCache.grids)
		{
			_chunksRendered += grid.render(_shader, camera);
		}
		_shader.end();
	}

	public int getChunksRendered()
	{
		return _chunksRendered;
	}
}
