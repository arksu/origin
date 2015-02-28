package com.a4server.gameserver.network.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 28.02.15.
 */
public class ObjectInteractive extends GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ObjectInteractive.class.getName());

	private int _objectId;
	private boolean _value;

	public ObjectInteractive(int objectId, boolean value)
	{
		_objectId = objectId;
		_value = value;
	}

	@Override
	protected void write()
	{
		writeC(0x1B);
		writeD(_objectId);
		writeC(_value ? 1 : 0);
	}
}
