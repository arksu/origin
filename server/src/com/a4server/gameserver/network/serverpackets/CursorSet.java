package com.a4server.gameserver.network.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 12.08.16.
 */
public class CursorSet extends GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CursorSet.class.getName());

	private final String _name;

	public CursorSet(String name)
	{
		_name = name;
	}

	@Override
	protected void write()
	{
		writeC(0x22);
		writeS(_name);
	}
}
