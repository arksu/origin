package com.a4server.gameserver.network.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 17.02.15.
 */
public class CreatureSay extends GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CreatureSay.class.getName());

	private int _objectId;
	private String _message;

	public CreatureSay(int objectId, String message)
	{
		_objectId = objectId;
		_message = message;
	}

	@Override
	protected void write()
	{
		writeC(0x17);
		writeD(_objectId);
		writeS(_message);
	}
}
