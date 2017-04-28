package com.a4server.gameserver.model.position;

import com.a4server.gameserver.model.collision.Move;
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

	// TODO
	@Override
	protected int getToX()
	{
		return 0;
	}

	@Override
	protected int getToY()
	{
		return 0;
	}

	@Override
	protected Move.MoveType getMoveType()
	{
		return null;
	}

	@Override
	public GameServerPacket makeMovePacket()
	{
		return null;
	}
}
