package com.a2client.network.game.serverpackets;

import com.a2client.network.game.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 28.02.15.
 */
public class ObjectInteractive extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x1B, ObjectInteractive.class);
	}

	private static final Logger _log = LoggerFactory.getLogger(ObjectInteractive.class.getName());

	int _objectId;
	boolean _value;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_value = readC() == 1;
	}

	@Override
	public void run()
	{
		_log.debug("ObjectInteractive " + _objectId + " val=" + _value);
	}
}
