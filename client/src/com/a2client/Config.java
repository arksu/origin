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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.lwjgl.LwjglPreferences;
import org.apache.log4j.Logger;

public class Config
{
    private static final Logger _log = Logger.getLogger(Config.class);

    static
    {
        isFullscreen = false;
        WindowWidth = 640;
        WindowHeight = 480;
    }

    static public final String RESOURCE_DIR = "assets/";

    // имя конфиг файла в каталоге с клиентом
    public static final String CONFIG_FILE = "origin-world.prefs";
    // адрес логин сервера
    public static String login_server = "origin-world.com";
    // порт логин сервера
    public static int login_server_port = 2048;
    // раземры экрана
    public static int ScreenWidth;
    public static int ScreenHeight;
    // раземры окна
    public static int WindowWidth;
    public static int WindowHeight;
    // желаемое значение фпс
    public static int FrameFate;
    // включать ли вертикальную синхронизацию
    public static boolean vSync;
    // Снижение фоновой активности окна
    public static boolean ReduceInBackground;
    // запускать в полноэкранном режиме
    public static boolean isFullscreen;
    // размеры экрана для сохранения в конфиг файле (применятся при следующем запуске)
    public static int ScreenWidth_to_save;
    public static int ScreenHeight_to_save;
    // адрес сервера с переводами
    public static String lang_remote_host = "origin-world.com";
    // путь к файлу переводов
    public static String lang_path = "/lang/lang_?.txt";
    // юзер агент для http запросов
    public static String user_agent = "origin_client";
    // текущий язык
    public static String current_lang;
    // логин и пароль под которым заходит юзер на сервер
    public static String account;
    public static transient String password;
    // сохранять ли пароль
    public static boolean save_pass;

    public static final int PROTO_VERSION = 3;
    public static final int CLIENT_VERSION = 80;

    public static final int ICON_SIZE = 32;

    // режим дебага
    public static boolean debug = false;
    // показывать детальную расшифровку пакетов
    public static boolean debug_packets = false;
    // режим быстрого входа с последним вырбарным чаром
    public static boolean quick_login_mode = false;
    // загружать перевод
    public static boolean load_translate = true;

    public static void ParseCMD(String[] args)
    {
        try
        {
            int i = 0;
            while (i < args.length)
            {
                String arg = args[i];
                if (arg.equals("-d"))
                { // Debug mode. Format: "-d"
                    debug = true;
                }
                if (arg.equals("-r"))
                { // Debug pkt mode. Format: "-r"
                    debug_packets = true;
                }
                if (arg.equals("-s"))
                { // Change server. Format: "-s servername"
                    i++;
                    login_server = args[i];
                }
                if (arg.equals("-p"))
                { // Change server. Format: "-p port"
                    i++;
                    login_server_port = Integer.parseInt(args[i]);
                }
                if (arg.equals("-q"))
                { // Quick login mode. Format: "-q"
                    quick_login_mode = true;
                }
                if (arg.equals("-nl"))
                { // dont load translate. Format: "-nl"
                    load_translate = false;
                }
                //                if (arg.equals("-dev_tile"))
                //                { // Format: -dev_tile <filename tiles.xml>
                //                    dev_tile_mode = true;
                //                    i++;
                //                    TilesDebug.dev_tiles_xml = args[i];
                //                }
                i++;
            }
        }
        catch (Exception e)
        {
            _log.warn("parse_cmd Error: " + e.getMessage());
        }
    }

    public static void PrintHelpCommads()
    {
        _log.info("Use commands:");
        _log.info("    Debug mode. Format: -d");
        _log.info("    Debug pkt mode. Format: -r");
        _log.info("    Change login server. Format: -s <servername>");
        _log.info("    Change login server port. Format: -p <port>");
        _log.info("    Update mode. Format: -u");
        _log.info("    Local mode. Format: -l");
        _log.info("    Quick login mode. Format: -q");
        _log.info("    Developer mode: tiles debug. Format: -dev_tile <filename tiles.xml>");
    }

    public static void Apply()
    {
        //TODO: sound
        //        float val = SoundVolume;
        //        SoundStore.get().setSoundVolume(val / 100);
        //        val = MusicVolume;
        //        SoundStore.get().setMusicVolume(val / 100);
    }

    public static int getScreenWidth()
    {
        return isFullscreen ? ScreenWidth : WindowWidth;
    }

    public static int getScreenHeight()
    {
        return isFullscreen ? ScreenHeight : WindowHeight;
    }

    static protected Preferences getPrefs()
    {
        return new LwjglPreferences(CONFIG_FILE, ".prefs/");
    }

    public static void Load()
    {
        Preferences p = getPrefs();
        WindowWidth = p.getInteger("window_width", 1024);
        WindowHeight = p.getInteger("window_height", 650);
        ScreenWidth = p.getInteger("screen_width", 1024);
        ScreenHeight = p.getInteger("screen_height", 768);
        FrameFate = p.getInteger("frame_rate", 60);
        vSync = p.getBoolean("use_vsync", true);
        ReduceInBackground = p.getBoolean("reduce_bg", true);
        //        SoundVolume = AppSettings.getInt("sound_vol", 50);
        //        MusicVolume = AppSettings.getInt("music_vol", 50);
        //        ScreenWidth_to_save = ScreenWidth;
        //        ScreenHeight_to_save = ScreenHeight;
        isFullscreen = p.getBoolean("start_fullscreen", false);
        //        SoundEnabled = AppSettings.getBool("sound_enabled", true);
        //        DebugEngine = AppSettings.getBool("debug_engine", false);
        current_lang = p.getString("language", "en");

        //        count_objs = AppSettings.getBool("count_objs", true);
        //        hide_overlapped = AppSettings.getBool("hide_overlapped", true);
        //        move_inst_left_mouse = AppSettings.getBool("move_inst_left_mouse", true);
        //        zoom_by_wheel = AppSettings.getBool("zoom_by_wheel", true);
        //        zoom_over_mouse = AppSettings.getBool("zoom_over_mouse", true);
        //        fullscreen_alt_enter = AppSettings.getBool("fullscreen_alt_enter", true);
        //        minimap_draw_objects = AppSettings.getBool("minimap_draw_objects", false);
        account = p.getString("account", "");
        password = p.getString("password", "");
        //
        save_pass = p.getBoolean("save_pass", false);
    }

    public static void SaveOptions()
    {
        //        Preferences p = getPrefs();
        Preferences p = Gdx.app.getPreferences(CONFIG_FILE);
        p.putInteger("window_width", WindowWidth);
        p.putInteger("window_height", WindowHeight);
        p.putInteger("screen_width", ScreenWidth_to_save);
        p.putInteger("screen_height", ScreenHeight_to_save);
        p.putInteger("frame_rate", FrameFate);
        p.putBoolean("use_vsync", vSync);
        p.putBoolean("reduce_bg", ReduceInBackground);
        p.putBoolean("start_fullscreen", isFullscreen);
        //        AppSettings.put("sound_enabled", SoundEnabled);
        //        AppSettings.put("sound_vol", SoundVolume);
        //        AppSettings.put("music_vol", MusicVolume);
        //        AppSettings.put("debug_engine", DebugEngine);
        p.putString("language", current_lang);
        //        AppSettings.put("count_objs", count_objs);
        //        AppSettings.put("hide_overlapped", hide_overlapped);
        //        AppSettings.put("move_inst_left_mouse", move_inst_left_mouse);
        //        AppSettings.put("zoom_by_wheel", zoom_by_wheel);
        //        AppSettings.put("zoom_over_mouse", zoom_over_mouse);
        //        AppSettings.put("fullscreen_alt_enter", fullscreen_alt_enter);
        //        AppSettings.put("minimap_draw_objects", minimap_draw_objects);
        p.putString("account", account);
        if (save_pass)
        {
            p.putString("password", password);
        }
        else
        {
            p.putString("password", "");
        }
        p.putBoolean("save_pass", save_pass);

        p.flush();
    }
}
