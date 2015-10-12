package com.a4server.gameserver.network.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 13.10.15.
 */
public class Actions extends GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(Actions.class.getName());

	/**
	 * список действий в json
	 */
	String _actions;

	@Override
	protected void write()
	{
		writeC(0x20);
		writeS(_actions);
	}
}
