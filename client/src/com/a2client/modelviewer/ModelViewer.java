package com.a2client.modelviewer;

import com.a2client.Input;
import com.a2client.gui.GUIGDX;
import com.a2client.screens.ResourceLoader;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Version;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a2client.Main._input;

/**
 * просмотрщик моделей
 * Created by arksu on 12.03.17.
 */
public class ModelViewer extends com.badlogic.gdx.Game
{
	private static final Logger _log = LoggerFactory.getLogger(ModelViewer.class.getName());

	/**
	 * время за кадр в мс
	 */
	public static long DT;

	/**
	 * время за кадр в секундах
	 */
	public static float deltaTime;

	/**
	 * последний системный тик
	 */
	private static long _last_tick;

	public static void main(String[] args)
	{
		// пока отключим аудио
		LwjglApplicationConfiguration.disableAudio = true;

		// запускаем приложение
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Origin model viewer";
		cfg.useGL30 = true;
		cfg.vSyncEnabled = true;
		cfg.foregroundFPS = 60;
		cfg.backgroundFPS = 5;
//		cfg.samples = config._MSAASamples;
		cfg.width = 1024;
		cfg.height = 768;
		cfg.useHDPI = false;

		new LwjglApplication(new ModelViewer(), cfg);
	}

	@Override
	public void create()
	{
		_last_tick = System.currentTimeMillis();

		_input = new Input();
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Gdx.input.setInputProcessor(_input);

		_log.debug("LibGDX ver: " + Version.VERSION);
		_log.debug("gl vendor: " + Gdx.gl.glGetString(GL20.GL_VENDOR));
		_log.debug("gl ver: " + Gdx.gl.glGetString(GL20.GL_VERSION));

		ResourceLoader.loadSystemFont();
		GUIGDX.init();

		// экран загрузки ресурсов
		this.setScreen(new ViewScreen());
	}

	@Override
	public void render()
	{
		long now = System.currentTimeMillis();
		DT = now - _last_tick;
		_last_tick = now;
		deltaTime = DT / 1000f;

		_input.Update();
		super.render();
	}
}
