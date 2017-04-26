package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.collision.Move;
import com.a4server.gameserver.model.collision.VirtualObject;

/**
 * Created by arksu on 26.04.17.
 */
public class MoveToVirtual extends MoveController
{
	private final VirtualObject _virtual;

	public MoveToVirtual(VirtualObject virtual)
	{
		_virtual = virtual;
	}

	@Override
	protected int getToX()
	{
		return _virtual.getX();
	}

	@Override
	protected int getToY()
	{
		return _virtual.getY();
	}

	@Override
	protected Move.MoveType getMoveType()
	{
		return Move.MoveType.WALK;
	}

	@Override
	protected VirtualObject getVirtualObject()
	{
		return _virtual;
	}
}
