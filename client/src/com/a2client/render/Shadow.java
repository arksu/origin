package com.a2client.render;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 28.07.16.
 */
public class Shadow
{
	private static final Logger _log = LoggerFactory.getLogger(Shadow.class.getName());

	public static final int SHADOW_MAP_SIZE = 1024;

	public static final String VERTEX = "assets/shaders/shadowVertex.glsl";
	public static final String FRAGMENT = "assets/shaders/shadowFragment.glsl";

	private final DepthFrameBuffer _frameBuffer;

	private final ModelBatch _modelBatch;

	public Shadow()
	{
		_frameBuffer = new DepthFrameBuffer(Pixmap.Format.RGBA8888, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, true);
		_frameBuffer.createDepthTextre();
		ShadowShaderProvider shaderProvider = new ShadowShaderProvider();
		_modelBatch = new ModelBatch(shaderProvider);
	}

	public DepthFrameBuffer getFrameBuffer()
	{
		return _frameBuffer;
	}

	public ModelBatch getModelBatch()
	{
		return _modelBatch;
	}
}
