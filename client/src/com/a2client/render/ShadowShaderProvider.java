package com.a2client.render;

import com.a2client.Terrain;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.render.shadows.Shadow.FRAGMENT;
import static com.a2client.render.shadows.Shadow.VERTEX;

/**
 * Created by arksu on 28.07.16.
 */
public class ShadowShaderProvider extends DepthShaderProvider
{
	private static final Logger _log = LoggerFactory.getLogger(ShadowShaderProvider.class.getName());

	private DefaultShader.Config _config;

	public ShadowShaderProvider()
	{
		_config = new DefaultShader.Config(Gdx.files.internal(VERTEX).readString(), Gdx.files.internal(FRAGMENT).readString());
	}

	@Override
	protected Shader createShader(Renderable renderable)
	{
//		return new DefaultShader(renderable, _config);
		return new ShadowShader(renderable, Terrain._shaderShadow);
	}
}
