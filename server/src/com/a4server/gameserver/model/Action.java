package com.a4server.gameserver.model;

import com.a4server.ThreadPoolManager;

import java.util.concurrent.Future;

import static com.a4server.gameserver.GameTimeController.GAME_ACTION_PERIOD;

/**
 * действие которое делаем в данный момент времени
 * Created by arksu on 06.04.17.
 */
public class Action implements Runnable
{
	private final Human _actor;
	/**
	 * сколько всего тиков надо сделать
	 */
	int _totalCount;

	/**
	 * сколько тиков уже сделано
	 */
	int _count;

	/**
	 * код который надо выполнить по завершению
	 */
	Runnable _callback;

	/**
	 * объект с которым производим действие
	 */
	GameObject _target;

	Future<?> _task;

	boolean _isDone = false;

	public Action(Human actor, int totalCount, GameObject target, Runnable callback)
	{
		_actor = actor;
		_count = 0;
		_totalCount = totalCount;
		_callback = callback;
		_target = target;
	}

	public GameObject getTarget()
	{
		return _target;
	}

	public void start()
	{
		_task = ThreadPoolManager.getInstance().scheduleActionAtFixedRate(this, GAME_ACTION_PERIOD, GAME_ACTION_PERIOD);
	}

	public void stop()
	{
		if (!_isDone)
		{
			_task.cancel(false);
			_task = null;
		}
	}

	@Override
	public void run()
	{
		_count++;
		System.out.println("++++ "+_count);
		if (_count >= _totalCount && !_isDone)
		{
			stop();
			_isDone = true;
			_callback.run();
		}
	}
}
