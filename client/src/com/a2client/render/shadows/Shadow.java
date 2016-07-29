package com.a2client.render.shadows;

import com.a2client.render.DepthFrameBuffer;
import com.a2client.render.ShadowShaderProvider;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;
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

	private final ShadowBox _shadowBox;

	private Matrix4 lightViewMatrix;

	public Shadow(Camera camera)
	{
		_frameBuffer = new DepthFrameBuffer(Pixmap.Format.RGBA8888, SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, true);
		_frameBuffer.createDepthTextre(Texture.TextureFilter.Nearest, Texture.TextureWrap.ClampToEdge);
		ShadowShaderProvider shaderProvider = new ShadowShaderProvider();
		_modelBatch = new ModelBatch(shaderProvider);
		_shadowBox = new ShadowBox(lightViewMatrix, camera);
	}

	public void update()
	{
		_shadowBox.update();
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
