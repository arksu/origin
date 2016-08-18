package com.a2client.screens;

import com.a2client.Lang;
import com.a2client.Main;
import com.a2client.gui.GUI;
import com.a2client.gui.GUIGDX;
import com.a2client.gui.Skin;
import com.a2client.gui.Skin_MyGUI;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

import static com.a2client.Config.RESOURCE_DIR;

public class ResourceLoader implements Screen
{
	private static final Logger _log = LoggerFactory.getLogger(ResourceLoader.class.getName());

	public static BitmapFont systemFont;
	private State _state;
	private int _state_timer;

	private enum State
	{
		FADEIN,
		LOADING,
		FADEOUT
	}

	public ResourceLoader()
	{
		loadAll();
		_state = State.FADEIN;
		_state_timer = 0;
	}

	protected Class<?> getResourceType(String s)
	{
		// тип ресурса смотрим по расширению
		if (s.contains(".pack"))
		{
			return TextureAtlas.class;
		}

		else if (s.contains(".png"))
		{
			return Texture.class;
		}

		else if (s.contains(".fnt"))
		{
			return BitmapFont.class;
		}

		return Object.class;
	}

	@Override
	public void render(float delta)
	{
		if (_state == State.FADEIN)
		{
			_state_timer += Main.DT;

			if (_state_timer >= 400)
			{
				_state = State.LOADING;
			}
		}

		if (_state == State.FADEOUT)
		{
			_state_timer -= Main.DT;
			if (_state_timer < 0)
			{
				PostLoad();
				return;
			}
		}

		// все ресурсы загружены?
		if (_state == State.LOADING)
		{
			boolean ready = Main.getAssetManager().update();
			if (ready && _state == State.LOADING)
			{
				_state = State.FADEOUT;
			}
		}

		float t = 0.7f * ((float) _state_timer / 1000f);

		Gdx.gl.glClearColor(t, t, t, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		GUIGDX.getSpriteBatch().begin();

		float progress = Main.getAssetManager().getProgress();
		int percent = (int) (progress * 100f);
		//        if (_state == State.LOADING || _state == State.FADEIN) {
		GUIGDX.Text(null, (Gdx.graphics.getWidth() - 100) / 2, (Gdx.graphics.getHeight() / 2) - 50,
					Lang.getTranslate("LoadingScreen.loading") + " " + percent,
					new Color(t * 2.5f, t * 2.5f, t * 2.5f, 1f));
		//        }
		GUIGDX.getSpriteBatch().end();
	}

	protected void loadAll()
	{
		try
		{
			// читаем файл со списком критично важных ресурсов игры
			LineNumberReader reader = new LineNumberReader(new FileReader(RESOURCE_DIR + "files.txt"));
			while (true)
			{
				// построчно
				String s = reader.readLine();
				// если все уже прочитали
				if (s == null)
				{
					break;
				}
				s = s.toLowerCase();

				Main.getAssetManager().load(RESOURCE_DIR + s, getResourceType(s));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

	protected void PostLoad()
	{
		Skin_MyGUI skin = new Skin_MyGUI();
		skin.Init();
		skin.parseIcons();
		Skin.setInstance(skin);

		Login.Show();
	}

	/**
	 * загрузить системный фонт. ждем пока не загрузится
	 */
	public static void loadSystemFont()
	{
		// если фонт уже загружен - выйдем
		if (systemFont != null)
		{
			return;
		}

		systemFont = null;

		try
		{
			// системный шрифт генерим
			FreeTypeFontGenerator generator = new FreeTypeFontGenerator(new FileHandle(new File(RESOURCE_DIR + "dejavu.ttf")));
			FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
			parameter.size = 12;
			parameter.color = Color.WHITE;
			parameter.hinting = FreeTypeFontGenerator.Hinting.Full;
			parameter.renderCount = 4;
			systemFont = generator.generateFont(parameter);
			return;
		}
		catch (Exception e)
		{
			_log.warn("failed generate system font with freetype lib");
		}

		if (systemFont == null)
		{
			// только если есть файл и можем его прочесть
			File f = new File(RESOURCE_DIR + "system.fnt");
			if (f.exists() && f.canRead())
			{
				Main.getAssetManager().load(RESOURCE_DIR + "system.fnt", BitmapFont.class);
				// ждем окончания загрузки
				while (true)
				{
					if (Main.getAssetManager().update(100))
					{
						systemFont = Main.getAssetManager().get(RESOURCE_DIR + "system.fnt");
						break;
					}
				}
			}
		}
		else
		{
			throw new RuntimeException("error on generate system font");
		}
	}

	@Override
	public void resize(int width, int height)
	{
		GUI.getInstance().ResolutionChanged();
	}

	@Override
	public void show()
	{

	}

	@Override
	public void hide()
	{

	}

	@Override
	public void pause()
	{

	}

	@Override
	public void resume()
	{

	}

	@Override
	public void dispose()
	{

	}

}
