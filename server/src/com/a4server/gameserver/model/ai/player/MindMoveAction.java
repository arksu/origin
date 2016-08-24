package com.a4server.gameserver.model.ai.player;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.event.Event;
import com.a4server.gameserver.model.position.MoveToPoint;

/**
 * поведение для движения к объекту и взаимодействия с ним
 * Created by arksu on 28.02.15.
 */
public class MindMoveAction extends PlayerMind
{
	private int _targetObjectId;

	private final ArrivedCallback _arrivedCallback;

	public interface ArrivedCallback
	{
		void onArrived(CollisionResult moveResult);
	}

	public MindMoveAction(Player player, int objectId, ArrivedCallback callback)
	{
		super(player);
		_targetObjectId = objectId;
		_arrivedCallback = callback;
	}

	@Override
	public void onArrived(CollisionResult moveResult)
	{
		if (_arrivedCallback != null)
		{
			_arrivedCallback.onArrived(moveResult);
		}
	}

	@Override
	public void onTick()
	{
	}

	@Override
	public void handleEvent(Event event)
	{
	}

	@Override
	public void begin()
	{
		GameObject object = _player.isKnownObject(_targetObjectId);
		if (object.getPos().getDistance(_player.getPos()) < 2000)
		{
			_player.StartMove(new MoveToPoint(object.getPos()._x, object.getPos()._y));
		}
	}
}
