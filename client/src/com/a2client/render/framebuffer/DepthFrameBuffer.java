package com.a2client.render.framebuffer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.badlogic.gdx.Gdx.gl;

/**
 * фрейм буфер с возможностью получения буфера глубины в текстуре
 * Created by arksu on 27.07.16.
 */
public class DepthFrameBuffer extends CustomFrameBuffer
{
	private static final Logger _log = LoggerFactory.getLogger(DepthFrameBuffer.class.getName());

	private int _depthTexture = -1;

	public DepthFrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth)
	{
		super(format, width, height, hasDepth);
	}

	public void createDepthTextre()
	{
		createDepthTextre(Texture.TextureFilter.Linear, Texture.TextureWrap.Repeat);
	}

	public void createDepthTextre(Texture.TextureFilter filter, Texture.TextureWrap wrap)
	{
		// сначала биндим наш буфер
		gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, getFramebufferHandle());

		// создаем текстуру
		_depthTexture = gl.glGenTexture();
		// биндим ее
		gl.glBindTexture(GL11.GL_TEXTURE_2D, _depthTexture);
		gl.glTexImage2D(
				GL11.GL_TEXTURE_2D, 0,
				GL14.GL_DEPTH_COMPONENT16, width, height,
				0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, null);
		// сделаем сглаживание
		gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter.getGLEnum());
		gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter.getGLEnum());
		gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, wrap.getGLEnum());
		gl.glTexParameteri(GL11.GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, wrap.getGLEnum());
		// связываем нашу текстуру с буфером
		gl.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
								  GL20.GL_TEXTURE_2D,
								  _depthTexture, 0);

		// а вот тут может случится ата-та. т.к. надо знать ид буфера экрана. см код libgdx
		gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, 0);
	}

	public void bindDepthTexture()
	{
		if (_depthTexture >= 0)
		{
			Gdx.gl.glBindTexture(GL11.GL_TEXTURE_2D, _depthTexture);
		}
	}
}
