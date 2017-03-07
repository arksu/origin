package com.a4server.gameserver.model.position;

import com.a4server.gameserver.network.serverpackets.GameServerPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * двигаться на привязке к указанному объекту. всегда стремится догнать его
 * Created by arksu on 09.02.15.
 */
public class MoveFollow extends MoveController
{
	private static final Logger _log = LoggerFactory.getLogger(MoveFollow.class.getName());

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
