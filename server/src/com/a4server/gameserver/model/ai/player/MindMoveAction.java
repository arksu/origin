package com.a4server.gameserver.model.ai.player;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.event.Event;
import com.a4server.gameserver.model.position.MoveToObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a4server.gameserver.model.collision.CollisionResult.CollisionType.COLLISION_OBJECT;

/**
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
				// надо провести взаимодействие с этим объектом
				_log.debug("interact with object " + object.toString());
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
	public void free()
	{

	}

	@Override
	public void begin()
	{
		_player.StartMove(new MoveToObject(_targetObjectId));
	}
}
