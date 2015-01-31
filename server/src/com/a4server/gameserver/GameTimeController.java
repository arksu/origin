package com.a4server.gameserver;

import com.a4server.Config;
import com.a4server.util.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 08.01.2015.
 */
public class GameTimeController extends Thread
{
    protected static final Logger _log = LoggerFactory.getLogger(GameTimeController.class.getName());

    /**
     * игровой тик на обновление объектов, сколько обычных тиков
     */
    public static final int GAMETICK_PERIOD = 50;
    /**
     * игровых тиков в реальной секунде (на передвижение)
     */
    public static final int TICKS_PER_SECOND = 10;
    public static final int MILLIS_IN_TICK = 1000 / TICKS_PER_SECOND;
    /**
     * игровых дней в реальных сутках
     */
    public static final int IG_DAYS_PER_DAY = 4; // 1 день = 6 часов
    public static final int MILLIS_PER_IG_DAY = (3600000 * 24) / IG_DAYS_PER_DAY;
    public static final int SECONDS_PER_IG_DAY = MILLIS_PER_IG_DAY / 1000;
    public static final int MINUTES_PER_IG_DAY = SECONDS_PER_IG_DAY / 60;
    /**
     * игровых тиков в игровом дне
     */
    public static final int TICKS_PER_IG_DAY = SECONDS_PER_IG_DAY * TICKS_PER_SECOND;
    public static final int TICKS_PER_IG_MINUTE = TICKS_PER_IG_DAY / (24 * 60);

    private static GameTimeController _instance;
    private volatile int _tickCount;

    private GameTimeController()
    {
        super("GameTimeController");
        super.setDaemon(true);
        super.setPriority(MAX_PRIORITY);

        load();

        super.start();
    }

    public final int getTickCount()
    {
        return _tickCount;
    }

    /**
     * The true GameTime tick.
     */
    public final int getGameTicks()
    {
        return _tickCount / GAMETICK_PERIOD;
    }

    /**
     * количество абсолютных игровых минут
     *
     * @return
     */
    public final int getGameTime()
    {
        return (getTickCount() % TICKS_PER_IG_DAY) / TICKS_PER_IG_MINUTE;
    }

    public final int getGameHour()
    {
        return getGameTime() / 60;
    }

    public final int getGameMinute()
    {
        return getGameTime() % 60;
    }

    public final void stopTimer()
    {
        super.interrupt();
        _log.info("Stopping " + getClass().getSimpleName());
    }

    private void moveObjects() throws Exception
    {
        //        sleep(1000);
    }

    /**
     * загрузить значение тиков из базы
     */
    public void load()
    {
        _tickCount = GlobalVariablesManager.getInstance().getVarInt("server_time");
        if (_tickCount < 0)
        {
            _tickCount = 0;
        }
        _log.info("Server time: " + _tickCount + " ticks");
    }

    /**
     * сохранить значение тиков в базу
     */
    public void store()
    {
        if (Config.DEBUG)
        {
            _log.debug("store time in db: "+_tickCount);
        }
        GlobalVariablesManager.getInstance().saveVarInt("server_time", _tickCount);
    }

    public static void init()
    {
        _instance = new GameTimeController();
    }

    public static GameTimeController getInstance()
    {
        return _instance;
    }

    @Override
    public final void run()
    {
        _log.info(getClass().getSimpleName() + ": Started.");

        long nextTickTime, sleepTime;
        int gameTickTimer = getGameTicks();
        //        boolean isNight = isNight();

        // делаем что-то ночью. спавним кого-то
        //        if (isNight)
        //        {
        //            ThreadPoolManager.getInstance().executeAi(new Runnable()
        //            {
        //                @Override
        //                public final void run()
        //                {
        //                    DayNightSpawnManager.getInstance().notifyChangeMode();
        //                }
        //            });
        //        }

        while (true)
        {
            nextTickTime = ((System.currentTimeMillis() / MILLIS_IN_TICK) * MILLIS_IN_TICK) + 100;

            try
            {
                moveObjects();
            }
            catch (final Throwable e)
            {
                StackTrace.displayStackTraceInformation(e);
            }

            sleepTime = nextTickTime - System.currentTimeMillis();
            if (sleepTime > 0)
            {
                try
                {
                    Thread.sleep(sleepTime);
                    _tickCount++;
                    if (getGameTicks() - gameTickTimer > 10)
                    {
                        // запишем значение времени в базу
//                        ThreadPoolManager.getInstance().executeAi(new Runnable()
//                        {
//                            @Override
//                            public final void run()
//                            {
//                                store();
//                            }
//                        });
                        gameTickTimer = getGameTicks();
                        // игровой тик сменился. надо обсчитывать объекты
                    }
                }
                catch (final InterruptedException e)
                {
                    _log.warn("GameTimeController interrupted");
                }
            }

            //            if (isNight() != isNight)
            //            {
            //                isNight = !isNight;
            //
            //                ThreadPoolManager.getInstance().executeAi(new Runnable()
            //                {
            //                    @Override
            //                    public final void run()
            //                    {
            //                        DayNightSpawnManager.getInstance().notifyChangeMode();
            //                    }
            //                });
            //            }
        }
    }
}