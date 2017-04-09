package com.a4server.gameserver.model;

import com.a4server.ThreadPoolManager;
import com.a4server.gameserver.network.serverpackets.ActionProgress;

import java.util.concurrent.Future;

import static com.a4server.gameserver.GameTimeController.GAME_ACTION_PERIOD;

/**
 * действие которое делаем в данный момент времени
 * Created by arksu on 06.04.17.
 */
public class Action implements Runnable
{
	private final Human _actor;

	private final Player _player;

	/**
	 * сколько всего тиков надо сделать
	 */
	private int _totalCount;

	/**
	 * сколько тиков уже сделано
	 */
	private int _count;

	/**
	 * код который надо выполнить по завершению
	 */
	private Runnable _callback;

	/**
	 * объект с которым производим действие
	 */
	private GameObject _target;

	private Future<?> _task;

	private boolean _isDone = false;

	public Action(Human actor, int totalCount, GameObject target, Runnable callback)
	{
		_actor = actor;
		if (actor instanceof Player)
		{
			_player = ((Player) actor);
		}
		else
		{
			_player = null;
		}
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
		if (_player != null)
		{
			_player.getClient().sendPacket(new ActionProgress(_target.getObjectId(), _count, _totalCount));
		}
	}

	public void stop()
	{
		if (!_isDone)
		{
			_task.cancel(false);
			_task = null;
			if (_player != null)
			{
				_player.getClient().sendPacket(ActionProgress.EMPTY);
			}
		}
	}

	@Override
	public void run()
	{
		_count++;
		System.out.println("++++ " + _count);
		if (_count > _totalCount && !_isDone)
		{
			stop();
			_isDone = true;
			_callback.run();
		}
		else
		{
			if (_player != null)
			{
				_player.getClient().sendPacket(new ActionProgress(_target.getObjectId(), _count, _totalCount));
			}
		}
	}
}
