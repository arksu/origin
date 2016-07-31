package com.a2client.render.postprocess;

import com.a2client.render.framebuffer.DepthFrameBuffer;
import org.lwjgl.opengl.GL13;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.render.Render.makeShader;
import static com.badlogic.gdx.Gdx.gl;

/**
 * Created by arksu on 31.07.16.
 */
public class DepthOfFieldEffect extends Effect
{
	private static final Logger _log = LoggerFactory.getLogger(DepthOfFieldEffect.class.getName());

	public DepthOfFieldEffect()
	{
		this(false);
	}

	public DepthOfFieldEffect(boolean isFinal)
	{
		super(isFinal);
		_shaderProgram = makeShader("postprocess/dof", "postprocess/dof");
	}

	@Override
	public void bindTextures(DepthFrameBuffer frameBuffer)
	{
		// биндим и указываем текстуру которую выводим на экран / обрабатываем текущим эффектом
		gl.glActiveTexture(GL13.GL_TEXTURE0);
		frameBuffer.getColorBufferTexture().bind();

		gl.glActiveTexture(GL13.GL_TEXTURE7);
		frameBuffer.bindDepthTexture();

		_shaderProgram.setUniformi("u_texture", 0);
		getShaderProgram().setUniformi("u_textureDepth", 7);

	}
}
