package com.a4server.gameserver.network.packets.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 12.08.16.
 */
public class CursorSet extends GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CursorSet.class.getName());

	private final String _name;
	private final int _typeId;

	public CursorSet(String name, int typeId)
	{
		_name = name;
		_typeId = typeId;
	}

	@Override
	protected void write()
	{
		writeC(0x22);
		writeS(_name);
		writeD(_typeId);
	}
}
