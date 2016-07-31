package com.a2client.render.postprocess;

import com.a2client.render.DepthFrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

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

	public Effect(boolean isFinal)
	{
		_isFinal = isFinal;
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
}
