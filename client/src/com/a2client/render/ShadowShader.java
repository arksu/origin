package com.a2client.render;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 29.07.16.
 */
public class ShadowShader extends BaseShader
{
	private static final Logger _log = LoggerFactory.getLogger(ShadowShader.class.getName());

	public Renderable renderable;

	public ShadowShader(final Renderable renderable, final ShaderProgram shaderProgram)
	{
		this.renderable = renderable;
		this.program = shaderProgram;
		register(DefaultShader.Inputs.worldTrans, DefaultShader.Setters.worldTrans);
		register(DefaultShader.Inputs.projViewTrans, DefaultShader.Setters.projViewTrans);
//		register(DefaultShader.Inputs.normalMatrix, DefaultShader.Setters.normalMatrix);

	}

	@Override
	public void init()
	{
		final ShaderProgram program = this.program;
		this.program = null;
		init(program, renderable);
		renderable = null;
	}

	@Override
	public int compareTo(Shader other)
	{
		return 0;
	}

	@Override
	public boolean canRender(Renderable instance)
	{
		return true;
	}

	@Override
	public void render(Renderable renderable, Attributes combinedAttributes)
	{
//		context.setCullFace(GL_BACK);
		// Classic depth test
		context.setDepthTest(GL20.GL_LEQUAL);
		// Deactivate blending on first pass
		context.setBlending(false, GL20.GL_ONE, GL20.GL_ONE);

		context.setDepthMask(true);

		super.render(renderable, combinedAttributes);


//		program.begin();
//		super.render(renderable, combinedAttributes);
//		program.end();
	}
}
