package com.a2client.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.render.Render.SHADER_VERSION;

/**
 * своя реализация шейдеров для моделей, дефолтные не канают ни разу!
 * Created by arksu on 24.07.16.
 */
public class ModelShaderProvider extends BaseShaderProvider
{
	private static final Logger _log = LoggerFactory.getLogger(ModelShaderProvider.class.getName());

	public static final String VERTEX = "assets/shaders/modelVertex.glsl";
	public static final String FRAGMENT = "assets/shaders/modelFragment.glsl";

	private DefaultShader.Config _config;

	public ModelShaderProvider()
	{
		_config = new DefaultShader.Config(Gdx.files.internal(VERTEX).readString(), Gdx.files.internal(FRAGMENT).readString());
	}

	@Override
	protected Shader createShader(Renderable renderable)
	{
		return new ModelShader(renderable, _config);
	}

	@Override
	public Shader getShader(Renderable renderable)
	{
		// небольшой такой КОСТЫЛЬ из за убогости LibGDX
		// явно укажем версию шейдеров. и немного поправим совместимость...
		ShaderProgram.prependVertexCode =
				SHADER_VERSION + "\n"
				+ "#define varying out\n"
				+ "#define attribute in\n";

		ShaderProgram.prependFragmentCode =
				SHADER_VERSION + "\n"
				+ "#define varying in\n"
				+ "#define texture2D texture\n"
				+ "#define gl_FragColor fragColor\n"
				+ "out vec4 fragColor;\n";

		Shader shader = super.getShader(renderable);

		ShaderProgram.prependVertexCode = null;
		ShaderProgram.prependFragmentCode = null;

		return shader;
	}
}
