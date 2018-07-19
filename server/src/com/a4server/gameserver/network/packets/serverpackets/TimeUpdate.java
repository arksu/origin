package com.a4server.gameserver.network.packets.serverpackets;

import com.a4server.gameserver.GameTimeController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 08.01.2015.
 */
public class TimeUpdate extends GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(TimeUpdate.class.getName());

	@Override
	protected void write()
	{
		writeC(0x0C);
		// игровые минуты с начала мира
		writeD(GameTimeController.getInstance().getGameTime());
		// температура воздуха
		writeC(25);
		// погода
		writeC(0);
	}
}
