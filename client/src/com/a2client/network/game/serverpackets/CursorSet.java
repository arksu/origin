package com.a2client.network.game.serverpackets;

import com.a2client.Cursor;
import com.a2client.network.game.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 12.08.16.
 */
public class CursorSet extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x22, CursorSet.class);
	}

	private static final Logger _log = LoggerFactory.getLogger(CursorSet.class.getName());

	private String _name;

	private int _typeId;

	@Override
	public void readImpl()
	{
		_name = readS();
		_typeId = readD();
	}

	@Override
	public void run()
	{
		_log.debug("cursor set: " + _name + " : " + _typeId);
		Cursor.getInstance().setCursor(_name);
	}
}
