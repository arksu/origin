package com.a2client.render.postprocess;

import com.a2client.render.Render;
import com.a2client.render.framebuffer.DepthFrameBuffer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Vector2;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.Gdx.gl;

/**
 * пост процессинг. эффекты накладываем на финальный кадр
 * Created by arksu on 31.07.16.
 */
public class PostProcess
{
	/**
	 * квад для вывода на экран
	 */
	private final Mesh _fullScreenQuad;

	/**
	 * цепочка эффектов
	 */
	private final List<Effect> _effects = new ArrayList<>();

	/**
	 * буфер в который выводим изначальную картинку
	 */
	private DepthFrameBuffer _frameBuffer;

	public PostProcess()
	{
		_fullScreenQuad = Render.createFullScreenQuad();
	}

	public void doPostProcess()
	{
		// текущий буфер который обрабатываем
		DepthFrameBuffer frameBuffer = _frameBuffer;

		// пройдем по всем эффектам
		for (Effect effect : _effects)
		{
			// включим буфер эффекта если надо
			if (!effect.isFinal())
			{
				effect.getFrameBuffer().begin();
			}

			// включим шейдер
			effect.getShaderProgram().begin();

			effect.bindTextures(frameBuffer);

			// укажем размер экрана в шейдере
			effect.getShaderProgram().setUniformf("u_size", new Vector2(frameBuffer.getWidth(), frameBuffer.getHeight()));

			// выведем квад
			_fullScreenQuad.render(effect.getShaderProgram(), GL20.GL_TRIANGLE_STRIP);

			// выключим шейдер
			effect.getShaderProgram().end();

			gl.glBindTexture(GL11.GL_TEXTURE_2D, 0);

			if (!effect.isFinal())
			{
				effect.getFrameBuffer().end();
				frameBuffer = effect.getFrameBuffer();
			}
			else
			{
				// последний эффект прерывает цикл
				break;
			}
		}
		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);
	}

	public void onResize()
	{
		if (_frameBuffer != null)
		{
			_frameBuffer.dispose();
		}
		createBuffer();
	}

	public void createBuffer()
	{
		_frameBuffer = new DepthFrameBuffer(
				Pixmap.Format.RGBA8888,
				Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
				true, false, true
		);
		_frameBuffer.setHasDepthTexture(true);
		_frameBuffer.build();

	}

	public void dispose()
	{
		if (_frameBuffer != null)
		{
			_frameBuffer.dispose();
		}
	}

	public DepthFrameBuffer getFrameBuffer()
	{
		if (_frameBuffer == null) createBuffer();
		return _frameBuffer;
	}

	/**
	 * добавить эффект в цепочку
	 */
	public void addEffect(Effect effect)
	{
		_effects.add(effect);
	}

}
