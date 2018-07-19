package com.a4server.gameserver.network.packets.serverpackets;

/**
 * Created by arksu on 05.01.2015.
 */
public class ServerClose extends GameServerPacket
{
	public static final ServerClose STATIC_PACKET = new ServerClose();

	private ServerClose()
	{
	}

	@Override
	protected void write()
	{
		writeC(0x08);
	}
}
