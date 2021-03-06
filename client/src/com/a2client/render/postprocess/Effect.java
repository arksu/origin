package com.a2client.render.postprocess;

import com.a2client.render.framebuffer.DepthFrameBuffer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import org.lwjgl.opengl.GL13;

/**
 * эффект для пост процессинга
 * Created by arksu on 31.07.16.
 */
public class Effect
{
	/**
	 * буфер куда выводим картинку если это не последний эффект
	 */
	protected DepthFrameBuffer _frameBuffer;

	/**
	 * последний эффект в цепочке?
	 */
	protected boolean _isFinal;

	/**
	 * шейдер которым рендерим буфер
	 */
	protected ShaderProgram _shaderProgram;

	protected float _scale;

	public Effect()
	{
		this(false, 1f);
	}

	public Effect(boolean isFinal)
	{
		this(isFinal, 1f);
	}

	public Effect(boolean isFinal, float scale)
	{
		_isFinal = isFinal;
		_scale = scale;
		if (!_isFinal)
		{
			_frameBuffer = new DepthFrameBuffer(
					Pixmap.Format.RGBA8888,
					Math.round(Gdx.graphics.getWidth() * _scale),
					Math.round(Gdx.graphics.getHeight() * _scale),
					true, false, true);
			_frameBuffer.setHasDepthTexture(true);
			_frameBuffer.build();
		}
		_shaderProgram = null;
	}

	public ShaderProgram getShaderProgram()
	{
		return _shaderProgram;
	}

	public DepthFrameBuffer getFrameBuffer()
	{
		return _frameBuffer;
	}

	public void setFrameBuffer(DepthFrameBuffer frameBuffer)
	{
		_frameBuffer = frameBuffer;
	}

	public boolean isFinal()
	{
		return _isFinal;
	}

	public void bindTextures(DepthFrameBuffer frameBuffer)
	{
		getShaderProgram().setUniformf("u_texture", 0);
		// биндим и указываем текстуру которую выводим на экран / обрабатываем текущим эффектом
		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);
		frameBuffer.getColorBufferTexture(0).bind();
	}
}
