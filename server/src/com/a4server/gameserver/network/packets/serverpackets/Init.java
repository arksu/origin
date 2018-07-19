package com.a4server.gameserver.network.packets.serverpackets;

import com.a4server.Config;

/**
 * Created by arksu on 03.01.2015.
 */
public class Init extends GameServerPacket
{
	@Override
	protected void write()
	{
		writeC(0x01);
		// proto version
		writeC(Config.GAME_PROTO_VERSION);
	}
}
