package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.collision.CollisionResult;
import com.a4server.gameserver.model.collision.Move;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;
import com.a4server.gameserver.network.serverpackets.ObjectMove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * движение к заданному объекту
 * Created by arksu on 24.08.16.
 */
public class MoveToObject extends MoveController
{
	private static final Logger _log = LoggerFactory.getLogger(MoveToObject.class.getName());

	private final GameObject _object;

	public MoveToObject(GameObject object)
	{
		_object = object;
	}

	@Override
	protected int getToX()
	{
		return _object.getPos().getX();
	}

	@Override
	protected int getToY()
	{
		return _object.getPos().getY();
	}

	@Override
	protected Move.MoveType getMoveType()
	{
		return Move.MoveType.WALK;
	}

	@Override
	public boolean canStartMoving()
	{
		// объект был удален. двигатся больше некуда
		if (_object.isDeleting())
		{
			return false;
		}

		return super.canStartMoving();
	}

	@Override
	public boolean movingImpl(double dt)
	{
		// объект был удален. двигатся больше некуда
		if (_object.isDeleting())
		{
			return true;
		}

		return super.movingImpl(dt);
	}

	/**
	 * если прибыли в точку где находится объект и он не дал коллизий
	 * принудительно создадим коллизию, нужно для корректной работы логики с объектами которые не дают коллизий
	 */
	@Override
	protected void onArrived(int x, int y)
	{
		_activeObject.stopMove(new CollisionResult(_object, x, y));
	}

	/**
	 * создать пакет о передвижении объекта
	 * @return пакет
	 */
	@Override
	public GameServerPacket makeMovePacket()
	{
		return new ObjectMove(
				_activeObject.getObjectId(),
				_activeObject.getPos().getX(),
				_activeObject.getPos().getY(),
				getToX(),
				getToY(),
				(int) Math.round(_activeObject.getMoveSpeed())
		);
	}
}
