package com.a4server.gameserver.network.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 08.01.2015.
 */
public class WorldInfo extends GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(WorldInfo.class.getName());

	@Override
	protected void write()
	{
		writeC(0x10);
		// тип мира (пустыня, лес, север)
		writeC(0);
	}
}