package com.a2client.render.framebuffer;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLOnlyTextureData;
import com.badlogic.gdx.utils.GdxRuntimeException;
import org.lwjgl.opengl.GL11;

import static com.badlogic.gdx.graphics.Texture.TextureFilter.Linear;

/**
 * <p>
 * Encapsulates OpenGL ES 2.0 frame buffer objects. This is a simple helper class which should cover most FBO uses. It will
 * automatically create a texture for the color attachment and a renderbuffer for the depth buffer. You can get a hold of the
 * texture by {@link CustomFrameBuffer#getColorBufferTexture()}. This class will only work with OpenGL ES 2.0.
 * </p>
 *
 * <p>
 * FrameBuffers are managed. In case of an OpenGL context loss, which only happens on Android when a user switches to another
 * application or receives an incoming call, the framebuffer will be automatically recreated.
 * </p>
 *
 * <p>
 * A CustomFrameBuffer must be disposed if it is no longer needed
 * </p>
 * @author mzechner, realitix
 */
public class CustomFrameBuffer extends CustomGLFrameBuffer
{
	protected TextureFilter colorTextureFilter = Linear;
	protected TextureWrap colorTextureWrap = TextureWrap.ClampToEdge;

	protected TextureFilter depthTextureFilter = Linear;
	protected TextureWrap depthTextureWrap = TextureWrap.ClampToEdge;

	/**
	 * Creates a new CustomFrameBuffer having the given dimensions and potentially a depth buffer attached.
	 */
	public CustomFrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth)
	{
		this(format, width, height, hasDepth, false, true);
	}

	/**
	 * Creates a new CustomFrameBuffer having the given dimensions and potentially a depth and a stencil buffer attached.
	 * @param format the format of the color buffer; according to the OpenGL ES 2.0 spec, only RGB565, RGBA4444 and RGB5_A1 are
	 * color-renderable
	 * @param width the width of the framebuffer in pixels
	 * @param height the height of the framebuffer in pixels
	 * @param hasDepth whether to attach a depth buffer
	 * @throws com.badlogic.gdx.utils.GdxRuntimeException in case the CustomFrameBuffer could not be created
	 */
	public CustomFrameBuffer(Pixmap.Format format, int width, int height, boolean hasDepth,
							 boolean hasStencil, boolean hasColor)
	{
		super(format, width, height, hasDepth, hasStencil, hasColor, 1);
	}

	@Override
	protected Texture createColorTexture()
	{
		int glFormat = Pixmap.Format.toGlFormat(format);
		int glType = Pixmap.Format.toGlType(format);
		GLOnlyTextureData data = new GLOnlyTextureData(width, height, 0, glFormat, glFormat, glType);
		Texture result = new Texture(data);
		result.setFilter(colorTextureFilter, colorTextureFilter);
		result.setWrap(colorTextureWrap, colorTextureWrap);
		return result;
	}

	@Override
	protected Texture createDepthTexture()
	{
		int glFormat = GL11.GL_DEPTH_COMPONENT;
		int glType = GL11.GL_FLOAT;
		int internalFormat = depthBufferSize;
		GLOnlyTextureData data = new GLOnlyTextureData(width, height, 0, internalFormat, glFormat, glType);
		Texture result = new Texture(data);
		result.setFilter(depthTextureFilter, depthTextureFilter);
		result.setWrap(depthTextureWrap, depthTextureWrap);
		return result;
	}

	@Override
	protected void disposeTexture(Texture texture)
	{
		texture.dispose();
	}

	public void bindDepthTexture()
	{
		if (hasDepth && hasDepthTexture)
		{
			getDepthBufferTexture().bind();
		}
		else
		{
			throw new GdxRuntimeException("fbo havnt depth texture");
		}
	}

	public TextureFilter getColorTextureFilter()
	{
		return colorTextureFilter;
	}

	public void setColorTextureFilter(TextureFilter colorTextureFilter)
	{
		this.colorTextureFilter = colorTextureFilter;
	}

	public TextureWrap getColorTextureWrap()
	{
		return colorTextureWrap;
	}

	public void setColorTextureWrap(TextureWrap colorTextureWrap)
	{
		this.colorTextureWrap = colorTextureWrap;
	}

	public TextureFilter getDepthTextureFilter()
	{
		return depthTextureFilter;
	}

	public void setDepthTextureFilter(TextureFilter depthTextureFilter)
	{
		this.depthTextureFilter = depthTextureFilter;
	}

	public TextureWrap getDepthTextureWrap()
	{
		return depthTextureWrap;
	}

	public void setDepthTextureWrap(TextureWrap depthTextureWrap)
	{
		this.depthTextureWrap = depthTextureWrap;
	}

	/**
	 * See {@link GLFrameBuffer#unbind()}
	 */
	public static void unbind()
	{
		CustomGLFrameBuffer.unbind();
	}
}
