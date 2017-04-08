package com.a2client.network.game.serverpackets;

import com.a2client.network.game.GamePacketHandler;

/**
 * Created by arksu on 08.04.17.
 */
public class ActionProgress extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x25, ActionProgress.class);
	}

	@Override
	public void readImpl()
	{

	}

	@Override
	public void run()
	{

	}
}
