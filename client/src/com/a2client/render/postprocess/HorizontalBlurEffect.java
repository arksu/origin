package com.a2client.render.postprocess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.render.Render.makeShader;

/**
 * Created by arksu on 31.07.16.
 */
public class HorizontalBlurEffect extends Effect
{
	private static final Logger _log = LoggerFactory.getLogger(HorizontalBlurEffect.class.getName());

	public HorizontalBlurEffect(float scale)
	{
		this(false, scale);
	}

	public HorizontalBlurEffect(boolean isFinal, float scale)
	{
		super(isFinal, scale);
		_shaderProgram  = makeShader("postprocess/horizontalBlur", "postprocess/blur");
	}
}
