package com.a4server.gameserver.model.position;

import com.a4server.gameserver.network.serverpackets.GameServerPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 28.02.15.
 */
public class MoveToObject extends MoveController
{
	private static final Logger _log = LoggerFactory.getLogger(MoveToObject.class.getName());

	private int _targetObjectId;

	public MoveToObject(int targetObjectId)
	{
		_targetObjectId = targetObjectId;
	}

	@Override
	public boolean isMoving()
	{
		return false;
	}

	@Override
	public boolean canStartMoving()
	{
		return false;
	}

	@Override
	public GameServerPacket makeMovePacket()
	{
		return null;
	}

	@Override
	public boolean MovingImpl(double dt)
	{
		return false;
	}
}
