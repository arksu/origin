package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.collision.Move;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;
import com.a4server.gameserver.network.serverpackets.ObjectMove;

/**
 * движение объекта к заданной точке на карте
 * Created by arksu on 08.02.15.
 */
public class MoveToPoint extends MoveController
{
	/**
	 * куда движемся
	 */
	private int _toX;
	private int _toY;

	public MoveToPoint(int x, int y)
	{
		_toX = x;
		_toY = y;
	}

	@Override
	protected int getToX()
	{
		return _toX;
	}

	@Override
	protected int getToY()
	{
		return _toY;
	}

	@Override
	protected Move.MoveType getMoveType()
	{
		return Move.MoveType.WALK;
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
				_toX,
				_toY,
				(int) Math.round(_activeObject.getMoveSpeed())
		);
	}

}
