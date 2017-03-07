package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.network.serverpackets.GameServerPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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
	public boolean movingImpl(double dt)
	{
		return false;
	}
}
