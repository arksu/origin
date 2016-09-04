package com.a2client.render.postprocess;

import com.a2client.render.framebuffer.DepthFrameBuffer;
import com.badlogic.gdx.Gdx;
import org.lwjgl.opengl.GL13;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.render.Render.makeShader;

/**
 * Created by arksu on 31.07.16.
 */
public class OutlineEffect extends Effect
{
	private static final Logger _log = LoggerFactory.getLogger(OutlineEffect.class.getName());

	public OutlineEffect()
	{
		this(false);
	}

	public OutlineEffect(boolean isFinal)
	{
		super(isFinal);
		_shaderProgram  = makeShader("postprocess/outline", "postprocess/outline");
	}

	@Override
	public void bindTextures(DepthFrameBuffer frameBuffer)
	{
		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);
		frameBuffer.getColorBufferTexture(0).bind();

		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE1);
		frameBuffer.getColorBufferTexture(1).bind();

		getShaderProgram().setUniformi("u_texture", 0);
		getShaderProgram().setUniformi("u_textureDepth", 1);
	}
}
