package com.a2client.render.postprocess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.render.Render.makeShader;

/**
 * Created by arksu on 31.07.16.
 */
public class VerticalBlurEffect extends Effect
{
	private static final Logger _log = LoggerFactory.getLogger(VerticalBlurEffect.class.getName());

	public VerticalBlurEffect(float scale)
	{
		this(false, scale);
	}

	public VerticalBlurEffect(boolean isFinal, float scale)
	{
		super(isFinal, scale);
		_scale = scale;
		_shaderProgram = makeShader("postprocess/verticalBlur", "postprocess/blur");
	}
}
