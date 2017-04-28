package com.a4server.gameserver.model.ai.player;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.ai.ArrivedCallback;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.event.GridEvent;
import com.a4server.gameserver.model.position.MoveToObject;

import static com.a4server.gameserver.model.collision.CollisionResult.CollisionType.COLLISION_OBJECT;

/**
 * поведение для движения к объекту и взаимодействия с ним
 * Created by arksu on 28.02.15.
 */
public class MoveActionAI extends PlayerAI
{
	private final int _targetObjectId;

	private final ArrivedCallback _arrivedCallback;

	public MoveActionAI(Player player, int targetObjectId, ArrivedCallback callback)
	{
		super(player);
		_targetObjectId = targetObjectId;
		_arrivedCallback = callback;
	}

	@Override
	public void onArrived(CollisionResult moveResult)
	{
		if (_arrivedCallback != null
		    && moveResult.getResultType() == COLLISION_OBJECT
		    && moveResult.getObject() != null
		    && !moveResult.getObject().isDeleting()
		    && moveResult.getObject().getObjectId() == _targetObjectId)
		{
			_arrivedCallback.onArrived(moveResult);
		}
	}

	@Override
	public void onTick()
	{
	}

	@Override
	public void handleGridEvent(GridEvent gridEvent)
	{
	}

	@Override
	public void begin()
	{
		// проверим прилинкованный объект (возле которого уже стоим)
		GameObject linkedObject = _player.getLinkedObject();
		// если он совпадает с нашей целью - сразу вызовем каллбак
		if (linkedObject != null && linkedObject.getObjectId() == _targetObjectId)
		{
			_arrivedCallback.onArrived(new CollisionResult(linkedObject, -1, -1));
		}
		else
		{
			GameObject target = _player.getKnownKist().getKnownObjects().get(_targetObjectId);

			if (target.getPos().getDistance(_player.getPos()) < 3000)
			{
				CollisionResult result = _player.startMove(new MoveToObject(target));

				// движение почему то не началось, проверим коллизию - может мы уже там куда нам надо
				if (_player.getMoveController() == null)
				{
					onArrived(result);
				}
			}
		}
	}
}
