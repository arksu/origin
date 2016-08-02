package com.a2client.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Created by arksu on 28.07.16.
 */
public class ShadowShaderProvider extends DepthShaderProvider
{
	private final ShaderProgram _shaderProgram;

	public ShadowShaderProvider(ShaderProgram shaderProgram)
	{
		_shaderProgram = shaderProgram;
	}

	@Override
	protected Shader createShader(Renderable renderable)
	{
		return new ShadowShader(renderable, _shaderProgram);
	}
}
