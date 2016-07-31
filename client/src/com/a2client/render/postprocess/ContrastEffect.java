package com.a2client.render.postprocess;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.render.Render.makeShader;

/**
 * Created by arksu on 31.07.16.
 */
public class ContrastEffect extends Effect
{
	private static final Logger _log = LoggerFactory.getLogger(ContrastEffect.class.getName());

	public ContrastEffect()
	{
		this(false);
	}

	public ContrastEffect(boolean isFinal)
	{
		super(isFinal);
		_shaderProgram  = makeShader("postprocess/contrast", "postprocess/contrast");
	}
}
