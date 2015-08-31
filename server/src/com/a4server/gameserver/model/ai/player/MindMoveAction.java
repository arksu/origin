package com.a4server.gameserver.model.ai.player;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.event.Event;
import com.a4server.gameserver.model.position.MoveToPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a4server.gameserver.model.collision.CollisionResult.CollisionType.COLLISION_OBJECT;

/**
 * поведение для движения к объекту и взаимодействия с ним
 * Created by arksu on 28.02.15.
 */
public class MindMoveAction extends PlayerMind
{
	private static final Logger _log = LoggerFactory.getLogger(MindMoveAction.class.getName());

	private int _targetObjectId;

	public MindMoveAction(Player player, int objectId)
	{
		super(player);
		_targetObjectId = objectId;
	}

	@Override
	public void onArrived(CollisionResult moveResult)
	{
		// наша цель совпадает с тем куда пришли?
		if (moveResult.getResultType() == COLLISION_OBJECT)
		{
			GameObject object = moveResult.getObject();
			if (object != null && object.getObjectId() == _targetObjectId && !object.isDeleteing())
			{
				_log.debug("interact with object " + object.toString());
				// надо провести взаимодействие с этим объектом
				_player.beginInteract(object);
			}
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
