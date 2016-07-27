package com.a2client.render.water;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * буферы для отражений в воде
 * Created by arksu on 27.07.16.
 */
public class WaterFrameBuffers
{
	private static final Logger _log = LoggerFactory.getLogger(WaterFrameBuffers.class.getName());

	protected static final int REFLECTION_WIDTH = 320;
	private static final int REFLECTION_HEIGHT = 180;

	protected static final int REFRACTION_WIDTH = 1280;
	private static final int REFRACTION_HEIGHT = 720;

	private final FrameBuffer _reflectionFrameBuffer;
	private final FrameBuffer _refractionFrameBuffer;

	private boolean _cleared = false;

	public WaterFrameBuffers()
	{
		_reflectionFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, REFLECTION_WIDTH, REFLECTION_HEIGHT, true);
		_refractionFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, REFRACTION_WIDTH, REFRACTION_HEIGHT, true);
	}

	public FrameBuffer getReflectionFrameBuffer()
	{
		return _cleared ? null : _reflectionFrameBuffer;
	}

	public FrameBuffer getRefractionFrameBuffer()
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
