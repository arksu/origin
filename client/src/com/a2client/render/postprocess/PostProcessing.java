package com.a2client.render.postprocess;

import com.a2client.render.DepthFrameBuffer;
import com.a2client.render.Render;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Vector2;
import org.lwjgl.opengl.GL13;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * пост процессинг. эффекты накладываем на финальный кадр
 * Created by arksu on 31.07.16.
 */
public class PostProcessing
{
	private static final Logger _log = LoggerFactory.getLogger(PostProcessing.class.getName());

	private final Mesh _fullScreenQuad;

	private final List<Effect> _effects = new ArrayList<>();

	public PostProcessing()
	{
		_fullScreenQuad = Render.createFullScreenQuad();
	}

	public void doPostProcessing(DepthFrameBuffer initialFBO)
	{
		boolean first = true;
		for (Effect effect : _effects)
		{
			effect.getShaderProgram().begin();
			effect.getShaderProgram().setUniformf("u_size", new Vector2(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));

			if (first)
			{
				Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);
				initialFBO.getColorBufferTexture().bind();
				effect.getShaderProgram().setUniformf("u_texture", 0);
			}

			_fullScreenQuad.render(effect.getShaderProgram(), GL20.GL_TRIANGLE_STRIP);

			effect.getShaderProgram().end();
		}

	}

	public void addEffect(Effect effect)
	{
		_effects.add(effect);
	}

}
