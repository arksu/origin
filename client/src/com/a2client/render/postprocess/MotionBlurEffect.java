package com.a2client.render.postprocess;

import com.a2client.Main;
import com.a2client.render.framebuffer.DepthFrameBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.render.Render.makeShader;

/**
 * Created by arksu on 31.07.16.
 */
public class MotionBlurEffect extends Effect
{
	private static final Logger _log = LoggerFactory.getLogger(MotionBlurEffect.class.getName());

	private static final int MAX_OFFSET = 10;
	private static int currentOffset = 0;
	private static float time = 0;

	public MotionBlurEffect()
	{
		this(false);
	}

	public MotionBlurEffect(boolean isFinal)
	{
		super(isFinal);
		_shaderProgram = makeShader("postprocess/motionBlur");
	}

	@Override
	public void bindTextures(DepthFrameBuffer frameBuffer)
	{
		super.bindTextures(frameBuffer);

		time += Main.deltaTime;

		int c = (int) (time % (MAX_OFFSET+1));
		if (currentOffset != c)
		{
			currentOffset = c;
			_log.debug("offset: " + currentOffset);
		}

		getShaderProgram().setUniformi("u_offset", currentOffset);
	}
}
