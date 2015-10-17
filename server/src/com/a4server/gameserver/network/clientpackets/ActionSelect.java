package com.a4server.gameserver.network.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * игрок выбрал некое действие
 * Created by arksu on 18.10.15.
 */
public class ActionSelect extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ActionSelect.class.getName());

	private String _name;

	@Override
	public void readImpl()
	{
		_name = readS();
	}

	@Override
	public void run()
	{
		_log.debug("action: " + _name);
	}
}
