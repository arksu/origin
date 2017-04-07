package com.a4server.gameserver;

import com.a4server.ThreadPoolManager;
import com.a4server.gameserver.model.MovingObject;
import com.a4server.util.StackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * глобальное управление игровым временем
 * передвижения объектов
 * Created by arksu on 08.01.2015.
 */
public class GameTimeController extends Thread
{
	private static final Logger _log = LoggerFactory.getLogger(GameTimeController.class.getName());

	/**
	 * игровой тик на обновление объектов, сколько обычных тиков
	 */
	public static final int GAMETICK_PERIOD = 50;

	/**
	 * игровых тиков в реальной секунде (на передвижение)
	 */
	public static final int TICKS_PER_SECOND = 5;
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
	/**
	 * список объектов которые двигаются в данный момент, которые будем обновлять каждый тик
	 */
	private final Set<MovingObject> _movingObjects = ConcurrentHashMap.newKeySet();

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
	 * @return сколько всего игровых минут прошло
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

	/**
	 * передвинуть все объекты которые есть в списке
	 */
	private void moveObjects() throws Exception
	{
		_movingObjects.removeIf(MovingObject::updatePosition);
	}

	/**
	 * добавим объект который передвгиается
	 */
	public void addMovingObject(MovingObject object)
	{
		// проверим что контроллер есть у объекта, иначе нет смысла добавлять его в очередь передвижения
		if (object.getMoveController() == null)
		{
			return;
		}
		_movingObjects.add(object);
	}

	/**
	 * удалить объект из списка передвижений
	 * @param object объект
	 */
	public void removeMovingObject(MovingObject object)
	{
		_movingObjects.remove(object);
	}

	/**
	 * произошел игровой тик. надо обсчитать объекты. обновить их состояние
	 */
	private void doGameTick()
	{
//        _log.debug("doGameTick "+getGameTicks());
	}

	/**
	 * загрузить значение тиков из базы
	 */
	public void load()
	{
		// загрузим значение тиков из базы
		_tickCount = GlobalVariablesManager.getInstance().getVarInt("server_time");
		if (_tickCount < 0)
		{
			// если оно не корректно - поправим
			_tickCount = 0;
		}
		int secs = _tickCount / TICKS_PER_SECOND;
		int hours = secs / 3600;
		int days = hours / 24;
		_log.info("Server time: " + _tickCount + " ticks, real: "
		          + secs + " secs, " + days + " days and " + (hours % 24) + " hours");
	}

	/**
	 * сохранить значение тиков в базу
	 */
	private void store()
	{
		_log.debug("store time in db: " + _tickCount);
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
		int lastGameTick = getGameTicks();
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
			nextTickTime = ((System.currentTimeMillis() / MILLIS_IN_TICK) * MILLIS_IN_TICK) + MILLIS_IN_TICK;

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

					// если надо обсчитаем игровой тик
					if (getGameTicks() != lastGameTick)
					{
						doGameTick();
						lastGameTick = getGameTicks();
					}

					// раз в 10 игровых тиков сохраним состояние в базу
					if (getGameTicks() - gameTickTimer > 10)
					{
						// запишем значение времени в базу
						ThreadPoolManager.getInstance().scheduleGeneral(this::store, 0);
						gameTickTimer = getGameTicks();
					}
				}
				catch (final InterruptedException e)
				{
					_log.warn("GameTimeController: interrupted");
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