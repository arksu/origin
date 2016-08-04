package com.a2client.render.water;

import com.a2client.render.framebuffer.DepthFrameBuffer;
import com.badlogic.gdx.graphics.Pixmap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * буферы для отрисовки воды
 * Created by arksu on 27.07.16.
 */
public class WaterFrameBuffers
{
	private static final Logger _log = LoggerFactory.getLogger(WaterFrameBuffers.class.getName());

	protected static final int REFLECTION_WIDTH = 320;
	private static final int REFLECTION_HEIGHT = 180;

	protected static final int REFRACTION_WIDTH = 1280;
	private static final int REFRACTION_HEIGHT = 720;

	private final DepthFrameBuffer _reflectionFrameBuffer;
	private final DepthFrameBuffer _refractionFrameBuffer;

	private boolean _cleared = false;

	public WaterFrameBuffers()
	{
		_reflectionFrameBuffer = new DepthFrameBuffer(Pixmap.Format.RGBA8888, REFLECTION_WIDTH, REFLECTION_HEIGHT,
													  true, false, true);
		_reflectionFrameBuffer.build();

		_refractionFrameBuffer = new DepthFrameBuffer(Pixmap.Format.RGBA8888, REFRACTION_WIDTH, REFRACTION_HEIGHT,
													  true, false, true);
		_refractionFrameBuffer.setHasDepthTexture(true);
		_refractionFrameBuffer.build();
	}

	public DepthFrameBuffer getReflectionFrameBuffer()
	{
		return _cleared ? null : _reflectionFrameBuffer;
	}

	public DepthFrameBuffer getRefractionFrameBuffer()
	{
		return _cleared ? null : _refractionFrameBuffer;
	}

	public void clear()
	{
		_reflectionFrameBuffer.dispose();
		_refractionFrameBuffer.dispose();
		_cleared = true;
	}
}
