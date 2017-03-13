/*
 * This file is part of the Origin-World game client.
 * Copyright (C) 2013 Arkadiy Fattakhov <ark@ark.su>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.a2client;

import com.a2client.network.login.Crypt;
import com.a2client.util.Utils;
import com.a2client.util.scrypt.SCryptUtil;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl.LwjglPreferences;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config
{
	private static final Logger _log = LoggerFactory.getLogger(Config.class.getName());

	static public final String RESOURCE_DIR = "assets/";
	static public final String MODELS_DIR = RESOURCE_DIR + "models/";

	/**
	 * имя конфиг файла в каталоге с клиентом
	 */
	public static final String CONFIG_FILE = "origin-world.prefs";

	public static final int PROTOCOL_VERSION = 3;
	public static final int CLIENT_VERSION = 200;

	public static final int ICON_SIZE = 32;

	/**
	 * юзер агент для http запросов
	 */
	public static String USER_AGENT = "origin_client";

	/**
	 * адрес логин сервера
	 */
	@Option(name = "-s", usage = "login server address")
	public String _loginServer = "origin-world.com";

	@Option(name = "-g", usage = "game server address")
	public String _gameServer;

	/**
	 * порт логин сервера
	 */
	@Option(name = "-port", usage = "login server port")
	public int _loginServerPort = 2040;

	@Option(name = "-d", usage = "debug mode")
	public boolean _debug = false;

	/**
	 * показывать детальную расшифровку пакетов
	 */
	@Option(name = "-pkt", usage = "debug packets info")
	public boolean _debugPackets = false;

	/**
	 * режим быстрого входа с последним вырбарным чаром
	 */
	@Option(name = "-q", usage = "quick login")
	public boolean _quickLoginMode = false;

	/**
	 * логин и пароль под которым заходит юзер на сервер
	 */
	@Option(name = "-u", usage = "account")
	public String _account;
	@Option(name = "-p", usage = "password")
	public String _password;

	/**
	 * раземры экрана в полноэкранном режиме
	 */
	public int _screenWidth;
	public int _screenHeight;

	/**
	 * раземры окна в оконном режиме
	 */
	public int _windowWidth;
	public int _windowHeight;

	/**
	 * желаемое значение фпс
	 */
	public int _framePerSecond;

	/**
	 * включать ли вертикальную синхронизацию
	 */
	public boolean _vSync;

	/**
	 * Снижение фоновой активности окна
	 */
	public boolean _reduceInBackground;

	/**
	 * запускать в полноэкранном режиме
	 */
	public boolean _isFullscreen;
	/**
	 * размеры экрана для сохранения в конфиг файле (применятся при следующем запуске)
	 */
	public int _screenWidthToSave;
	public int _screenHeightToSave;

	/**
	 * текущий язык
	 */
	public String _currentLang;

	/**
	 * сохранять ли пароль
	 */
	public boolean _savePassword;

	/**
	 * cell shading?
	 */
	public boolean _renderOutline = false;

	/**
	 * сетку ладншафта выводить?
	 */
	public boolean _renderTerrainWireframe = false;

	/**
	 * рисовать красивую "продвинутую" воду
	 */
	public boolean _renderImproveWater = true;

	/**
	 * отрисовывать тени?
	 */
	public boolean _renderShadows = true;

	/**
	 * пост процессинг финального кадра
	 */
	public boolean _renderPostProcessing = true;

	/**
	 * MSAA сглаживание. сколько samples использовать?
	 * 0, 4, 8 и тд
	 */
	public int _MSAASamples = 0;

	private static Config _instance;

	public static Config getInstance()
	{
		if (_instance == null)
		{
			_instance = new Config();
		}
		return _instance;
	}

	public void parseArgs(String[] args)
	{
		CmdLineParser parser = new CmdLineParser(this);
		try
		{
			parser.parseArgument(args);
		}
		catch (CmdLineException e)
		{
			_log.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public static void apply()
	{
		//TODO: sound
		//        float val = SoundVolume;
		//        SoundStore.get().setSoundVolume(val / 100);
		//        val = MusicVolume;
		//        SoundStore.get().setMusicVolume(val / 100);
	}

	public int getScreenWidth()
	{
		return _isFullscreen ? _screenWidth : _windowWidth;
	}

	public int getScreenHeight()
	{
		return _isFullscreen ? _screenHeight : _windowHeight;
	}

	static protected Preferences getPrefs()
	{
		return new LwjglPreferences(CONFIG_FILE, ".prefs/");
	}

	public void loadOptions()
	{
		Preferences p = getPrefs();
		_windowWidth = p.getInteger("window_width", 1024);
		_windowHeight = p.getInteger("window_height", 650);
		_screenWidth = p.getInteger("screen_width", 1024);
		_screenHeight = p.getInteger("screen_height", 768);
		_framePerSecond = p.getInteger("frame_rate", 60);
		_vSync = p.getBoolean("use_vsync", true);
		_reduceInBackground = p.getBoolean("reduce_bg", true);
		//        SoundVolume = AppSettings.getInt("sound_vol", 50);
		//        MusicVolume = AppSettings.getInt("music_vol", 50);
		//        _screenWidthToSave = _screenWidth;
		//        _screenHeightToSave = _screenHeight;
		_isFullscreen = p.getBoolean("start_fullscreen", false);
		//        SoundEnabled = AppSettings.getBool("sound_enabled", true);
		//        DebugEngine = AppSettings.getBool("debug_engine", false);
		_currentLang = p.getString("language", "en_US");

		//        count_objs = AppSettings.getBool("count_objs", true);
		//        hide_overlapped = AppSettings.getBool("hide_overlapped", true);
		//        move_inst_left_mouse = AppSettings.getBool("move_inst_left_mouse", true);
		//        zoom_by_wheel = AppSettings.getBool("zoom_by_wheel", true);
		//        zoom_over_mouse = AppSettings.getBool("zoom_over_mouse", true);
		//        fullscreen_alt_enter = AppSettings.getBool("fullscreen_alt_enter", true);
		//        minimap_draw_objects = AppSettings.getBool("minimap_draw_objects", false);
		if (Utils.isEmpty(_account))
		{
			_account = p.getString("account", "");
		}
		if (Utils.isEmpty(_password))
		{
			_password = p.getString("password", "");
		}
		//
		_savePassword = p.getBoolean("save_pass", true);

//		_framePerSecond = 0;
//		_vSync = false;
	}

	public void saveOptions()
	{
		Preferences p = Gdx.app.getPreferences(CONFIG_FILE);
		p.putInteger("window_width", _windowWidth);
		p.putInteger("window_height", _windowHeight);
		p.putInteger("screen_width", _screenWidthToSave);
		p.putInteger("screen_height", _screenHeightToSave);
		p.putInteger("frame_rate", _framePerSecond);
		p.putBoolean("use_vsync", _vSync);
		p.putBoolean("reduce_bg", _reduceInBackground);
		p.putBoolean("start_fullscreen", _isFullscreen);
		//        AppSettings.put("sound_enabled", SoundEnabled);
		//        AppSettings.put("sound_vol", SoundVolume);
		//        AppSettings.put("music_vol", MusicVolume);
		//        AppSettings.put("debug_engine", DebugEngine);
		p.putString("language", _currentLang);
		//        AppSettings.put("count_objs", count_objs);
		//        AppSettings.put("hide_overlapped", hide_overlapped);
		//        AppSettings.put("move_inst_left_mouse", move_inst_left_mouse);
		//        AppSettings.put("zoom_by_wheel", zoom_by_wheel);
		//        AppSettings.put("zoom_over_mouse", zoom_over_mouse);
		//        AppSettings.put("fullscreen_alt_enter", fullscreen_alt_enter);
		//        AppSettings.put("minimap_draw_objects", minimap_draw_objects);
		p.putString("account", _account);
		// только если надо - сохраняем пароль
		if (_savePassword)
		{
			// если это уже хэш - спокойно его сохраняем
			if (Crypt.isPassowrdHash(_password))
			{
				p.putString("password", (_password));
			}
			else
			{
				// только если сервер уже прислал конфиг SCrypt
				// сохраним хэш от пароля
				if (Crypt.initialized())
				{
					p.putString("password", (SCryptUtil.scrypt(_password, Crypt.SCRYPT_N, Crypt.SCRYPT_R, Crypt.SCRYPT_P)));
				}
			}
		}
		else
		{
			// иначе затрем пароль
			p.putString("password", "");
		}
		p.putBoolean("save_pass", _savePassword);

		p.flush();
	}
}
