package com.a2client.gui;

import com.a2client.Main;
import com.a2client.util.Rect;
import com.a2client.util.Vec2i;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

import static com.a2client.Config.RESOURCE_DIR;
import static com.a2client.util.Align.*;

public class GUIGDX
{
	/**
	 * батчинг
	 */
	private static SpriteBatch _spriteBatch;

	/**
	 * пустая белая текстура 1x1 пиксель, для вывода прямоугольников различных
	 */
	private static Texture _emptyTexture;

	public static void init()
	{
		_spriteBatch = new SpriteBatch(1000, makeShader());
		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGB888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		_emptyTexture = new Texture(pixmap);
	}

	static public BitmapFont getFont(String name)
	{
		if (name.isEmpty())
		{
			return Main.getAssetManager().get(RESOURCE_DIR + "system.fnt");
		}
		else if (name.equals("default"))
		{
			return Main.getAssetManager().get(RESOURCE_DIR + "system.fnt");
		}
		else
		{
			return Main.getAssetManager().get(RESOURCE_DIR + name + ".fnt");
		}
	}

	static public void Text(String font, int x, int y, int w, int h, int align, String text, Color color)
	{
		int ax = x + (w - getTextWidth(font, text)) / 2;
		int ay = y + (h - getTextHeight(font, text)) / 2;
		if ((align & Align_HStretch) != Align_HStretch)
		{
			if ((align & Align_Left) > 0)
			{
				ax = x;
			}
			else if ((align & Align_Right) > 0)
			{
				ax = x + w - getTextWidth(font, text);
			}
		}
		if ((align & Align_VStretch) != Align_VStretch)
		{
			if ((align & Align_Top) > 0)
			{
				ay = y;
			}
			else if ((align & Align_Bottom) > 0)
			{
				ay = y + h - getTextHeight(font, text);
			}
		}
		Text(font, ax, ay, text, color);
	}

	static public void Text(String font, int x, int y, String text, Color color)
	{
		BitmapFont f = getFont(font);
		Color c = f.getColor();
		f.setColor(color);
		f.draw(_spriteBatch, text, (float) x, (float) (Gdx.graphics.getHeight() - y - 2));
		f.setColor(c);
	}

	static public void Text(String font, int x, int y, String text)
	{
		getFont(font).draw(_spriteBatch, text, (float) x, (float) (Gdx.graphics.getHeight() - y - 2));
	}

	static public int getTextWidth(String font, String text)
	{
		return (int) (new GlyphLayout(getFont(font), text).width);
	}

	static public int getTextHeight(String font, String text)
	{
		return (int) (new GlyphLayout(getFont(font), text).height + 5);
	}

	public static Vec2i getfontMetrics(String font, String text)
	{
		GlyphLayout tmp = new GlyphLayout(getFont(font), text);
		return new Vec2i((int) tmp.width, (int) tmp.height + 2);
	}

	static public void fillRect(Vec2i pos, Vec2i size, Color color)
	{
		Color c = _spriteBatch.getColor();
		_spriteBatch.setColor(color);
		_spriteBatch.draw(_emptyTexture, pos.x, Gdx.graphics.getHeight() - pos.y, size.x, -size.y);
		_spriteBatch.setColor(c);
	}

	static public void pushScissor(Rect s)
	{
		Rectangle clipBounds = new Rectangle(s.x, Gdx.graphics.getHeight() - s.y, s.w, -s.h);
		_spriteBatch.flush();
		ScissorStack.pushScissors(clipBounds);
	}

	static public void popScissor()
	{
		_spriteBatch.flush();
		ScissorStack.popScissors();
	}

	static public SpriteBatch getSpriteBatch()
	{
		return _spriteBatch;
	}

	static public ShaderProgram makeShader()
	{
		String vertexShader =
				"#version 140\n"

				+ "in vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
				+ "in vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
				+ "in vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
				+ "uniform mat4 u_projTrans;\n" //
				+ "out vec4 v_color;\n" //
				+ "out vec2 v_texCoords;\n" //
				+ "\n" //
				+ "void main()\n" //
				+ "{\n" //
				+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
				+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
				+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
				+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
				+ "}\n";
		String fragmentShader =
				"#version 140\n"
				+ "out vec4 fragColor;\n"

				+ "#ifdef GL_ES\n" //
				+ "#define LOWP lowp\n" //
				+ "precision mediump float;\n" //
				+ "#else\n" //
				+ "#define LOWP \n" //
				+ "#endif\n" //
				+ "in LOWP vec4 v_color;\n" //
				+ "in vec2 v_texCoords;\n" //
				+ "uniform sampler2D u_texture;\n" //
				+ "void main()\n"//
				+ "{\n" //
				+ "  fragColor = v_color * texture(u_texture, v_texCoords);\n" //
				+ "}";

		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (shader.isCompiled() == false)
		{
			throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		}
		return shader;
	}
}
