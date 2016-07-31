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
		getShaderProgram().setUniformf("u_texture", 0);
		// биндим и указываем текстуру которую выводим на экран / обрабатываем текущим эффектом
		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE0);
		frameBuffer.getColorBufferTexture().bind();

		getShaderProgram().setUniformf("u_textureDepth", 1);
		Gdx.gl.glActiveTexture(GL13.GL_TEXTURE1);
		frameBuffer.bindDepthTexture();
	}
}
