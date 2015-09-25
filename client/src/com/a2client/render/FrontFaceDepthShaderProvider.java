package com.a2client.render;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.BaseShaderProvider;

/**
 * Created by arksu on 24.09.15.
 */
public class FrontFaceDepthShaderProvider extends BaseShaderProvider
{
	public final FrontFaceDepthShader.Config config;

	public FrontFaceDepthShaderProvider (final FrontFaceDepthShader.Config config) {
		this.config = (config == null) ? new FrontFaceDepthShader.Config() : config;
	}

	public FrontFaceDepthShaderProvider (final String vertexShader, final String fragmentShader) {
		this(new FrontFaceDepthShader.Config(vertexShader, fragmentShader));
	}

	public FrontFaceDepthShaderProvider (final FileHandle vertexShader, final FileHandle fragmentShader) {
		this(vertexShader.readString(), fragmentShader.readString());
	}

	public FrontFaceDepthShaderProvider () {
		this(null);
	}

	@Override
	protected Shader createShader (final Renderable renderable) {
		return new FrontFaceDepthShader(renderable, config);
	}
}
