package com.a4server.gameserver.network.serverpackets;

/**
 * Created by arksu on 08.04.17.
 */
public class ActionProgress extends GameServerPacket
{
	@Override
	protected void write()
	{
		writeC(0x25);

	}
}
