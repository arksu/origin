package com.a2client;

import com.a2client.dialogs.Dialog;
import com.a2client.gui.GUI;
import com.a2client.gui.GUIGDX;
import com.a2client.network.Net;
import com.a2client.network.game.GamePacketHandler;
import com.a2client.screens.Login;
import com.a2client.screens.ResourceLoader;
import com.a2client.util.Utils;
import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Main extends com.badlogic.gdx.Game
{
	private static final Logger _log = LoggerFactory.getLogger(Main.class.getName());
	public static Input _input;

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

	private static AssetManager _assetManager;
	private static Main _instance;

	@Override
	public void resume()
	{
		Cursor.getInstance().setCursor("");
	}

	@Override
	public void create()
	{
		_instance = this;
		_assetManager = new AssetManager();
		_last_tick = System.currentTimeMillis();
		_input = new Input();
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		Gdx.input.setInputProcessor(_input);

		// установим дефолт курсор
		Cursor.getInstance().setCursor("");

		_log.debug("LibGDX ver: " + Version.VERSION);
		_log.debug("gl vendor: " + Gdx.gl.glGetString(GL20.GL_VENDOR));
		_log.debug("gl ver: " + Gdx.gl.glGetString(GL20.GL_VERSION));

		ResourceLoader.loadSystemFont();
		GUIGDX.init();

		// экран загрузки ресурсов
		this.setScreen(new ResourceLoader());
	}

	public static void main(String[] args)
	{
		// пока отключим аудио
		LwjglApplicationConfiguration.disableAudio = true;

		Utils.rotateLog();
		_log.info("Build: " + buildVersion());

		// прочтем аргументы командной строки
		Config config = Config.getInstance();
		config.parseArgs(args);
		config.loadOptions();
		Lang.setLocale(config._currentLang);
		Lang.loadTranslate();

		// загрузим нативные либы
		LoadNativeLibs();

		// инициализурем пакеты (заполним опкоды)
		GamePacketHandler.InitPackets();

		// запускаем приложение
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Origin " + buildVersion();
		cfg.addIcon("assets/a1_16.png", Files.FileType.Internal);
		cfg.addIcon("assets/a1_32.png", Files.FileType.Internal);
		cfg.addIcon("assets/a1_128.png", Files.FileType.Internal);
		cfg.useGL30 = true;
		cfg.vSyncEnabled = config._vSync;
		cfg.foregroundFPS = config._framePerSecond;
		cfg.backgroundFPS = config._reduceInBackground ? 5 : config._framePerSecond;
		cfg.samples = config._MSAASamples;
		cfg.width = config.getScreenWidth();
		cfg.height = config.getScreenHeight();

		new LwjglApplication(new Main(), cfg);
	}

	public static AssetManager getAssetManager()
	{
		return _assetManager;
	}

	public static String buildVersion()
	{
		return "v" + (Config.getInstance().CLIENT_VERSION / 100) + "." + (Config.getInstance().CLIENT_VERSION % 100);
	}

	@Override
	public void render()
	{
		update();
		super.render();

		// курсор выводим в самую последнюю очередь
		Cursor.getInstance().render();
	}

	private void update()
	{
		long now = System.currentTimeMillis();
		DT = now - _last_tick;
		_last_tick = now;
		deltaTime = DT / 1000f;

		Net.ProcessPackets();

		if (Net.getConnection() != null)
		{
			if (!Net.getConnection().isActive())
			{
				onDisconnected();
			}
		}

		_input.Update();
		GUI.getInstance().Update();
	}

	protected void onDisconnected()
	{
		Net.CloseConnection();
		Login.setStatus("disconnected");
		ReleaseAll();
	}

	static protected void LoadNativeLibs()
	{
		if (System.getProperty("os.name").contains("FreeBSD"))
		{
			System.load(new File("lib/native/freebsd/libgdx64.so").getAbsolutePath());
		}
	}

	static public Main getInstance()
	{
		return _instance;
	}

	static public void freeScreen()
	{
		Screen screen = getInstance().getScreen();
		if (screen != null)
		{
			screen.dispose();
		}
	}

	static public void ReleaseAll()
	{
		Config.getInstance().getInstance().saveOptions();
		Dialog.HideAll();
		ChatHistory.clear();

		Net.CloseConnection();

		freeScreen();
		getInstance().setScreen(new Login());
	}

}
