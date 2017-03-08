package com.a4server.gameserver.model.ai.player;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.event.GridEvent;
import com.a4server.gameserver.model.position.MoveToPoint;

/**
 * поведение для движения к объекту и взаимодействия с ним
 * Created by arksu on 28.02.15.
 */
public class MoveActionAI extends PlayerAI
{
	private int _targetObjectId;

	private final ArrivedCallback _arrivedCallback;

	public interface ArrivedCallback
	{
		void onArrived(CollisionResult moveResult);
	}

	public MoveActionAI(Player player, int objectId, ArrivedCallback callback)
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
	public void handleEvent(GridEvent gridEvent)
	{
	}

	@Override
	public void begin()
	{
		GameObject target = _player.getKnownKist().getKnownObjects().get(_targetObjectId);
		// TODO move to object

		if (target.getPos().getDistance(_player.getPos()) < 2000)
		{
			_player.startMove(new MoveToPoint(target.getPos()._x, target.getPos()._y));
		}
	}
}
