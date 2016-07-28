package com.a2client.render;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import static com.a2client.render.Render.SHADER_VERSION;

/**
 * Created by arksu on 24.09.15.
 */
public class DepthShaderProvider extends BaseShaderProvider
{
	public final DepthShader.Config config;

	public DepthShaderProvider(final DepthShader.Config config)
	{
		this.config = (config == null) ? new DepthShader.Config() : config;
	}

	public DepthShaderProvider()
	{
		this(null);
	}

	@Override
	protected Shader createShader(final Renderable renderable)
	{
		return new DepthShader(renderable, config);
	}

	@Override
	public Shader getShader(Renderable renderable)
	{
		// небольшой такой КОСТЫЛЬ из за убогости LibGDX
		// явно укажем версию шейдеров. и немного поправим совместимость...
		ShaderProgram.prependVertexCode =
				SHADER_VERSION + "\n";

		ShaderProgram.prependFragmentCode =
				SHADER_VERSION + "\n";

		Shader shader = super.getShader(renderable);

		ShaderProgram.prependVertexCode = null;
		ShaderProgram.prependFragmentCode = null;

		return shader;
	}
}
