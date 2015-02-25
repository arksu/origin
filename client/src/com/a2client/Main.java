package com.a2client;

import com.a2client.dialogs.Dialog;
import com.a2client.gui.GUI;
import com.a2client.gui.GUIGDX;
import com.a2client.network.Net;
import com.a2client.network.game.GamePacketHandler;
import com.a2client.screens.Login;
import com.a2client.screens.ResourceLoader;
import com.a2client.util.Utils;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Main extends com.badlogic.gdx.Game
{
    public static Logger _log = LoggerFactory.getLogger(Main.class.getName());
    public static Input _input;
    public static long DT;
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
        Gdx.input.setInputProcessor(_input);
        GUIGDX.Init();

        // установим дефолт курсор
        Cursor.getInstance().setCursor("");

        _log.debug("gl ver: " + Gdx.gl.glGetString(GL20.GL_VERSION));

        // экран загрузки ресурсов
        this.setScreen(new ResourceLoader());
    }

    public static void main(String[] args)
    {
        // пока отключим аудио
        LwjglApplicationConfiguration.disableAudio = true;

        Utils.RotateLog();
        Log.init();
        _log.info("Build: " + buildVersion());

        // прочтем аргументы командной строки
        Config.ParseCMD(args);
        Config.Load();
        Lang.LoadTranslate();

        // загрузим нативные либы
        LoadNativeLibs();

        GamePacketHandler.InitPackets();

        // запускаем приложение
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Origin v2.0";
        cfg.addIcon("assets/a1_16.png", Files.FileType.Internal);
        cfg.addIcon("assets/a1_32.png", Files.FileType.Internal);
        cfg.addIcon("assets/a1_128.png", Files.FileType.Internal);
        cfg.useGL30 = false;
        cfg.vSyncEnabled = false;//Config.vSync;
        cfg.foregroundFPS = 0;//Config.FrameFate;
        if (Config.ReduceInBackground)
        {
            cfg.backgroundFPS = 7;
        }
        else
        {
            cfg.backgroundFPS = Config.FrameFate;
        }
        cfg.width = Config.getScreenWidth();
        cfg.height = Config.getScreenHeight();

        new LwjglApplication(new Main(), cfg);
    }

    public static AssetManager getAssetManager()
    {
        return _assetManager;
    }

    public static String buildVersion()
    {
        return "ver 0." + Config.CLIENT_VERSION + "   revision: " + GameConst.svn_revision + "   date: " + GameConst.build_date;
    }

    @Override
    public void render()
    {
//        Cursor.getInstance().setCursor("");
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
        Config.SaveOptions();
        Dialog.HideAll();
        ChatHistory.clear();

        Net.CloseConnection();

        freeScreen();
        getInstance().setScreen(new Login());
    }

}
