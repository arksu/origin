package com.a2client.network.game.serverpackets;

import com.a2client.network.game.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusUpdate extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x0F, StatusUpdate.class);
	}

	private static final Logger _log = LoggerFactory.getLogger(StatusUpdate.class.getName());

	@Override
	public void readImpl()
	{

	}

	@Override
	public void run()
	{

	}
}
