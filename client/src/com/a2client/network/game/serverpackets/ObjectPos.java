package com.a2client.network.game.serverpackets;

import com.a2client.ObjectCache;
import com.a2client.network.game.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 11.02.15.
 */
public class ObjectPos extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x15, ObjectPos.class);
	}

	private static final Logger _log = LoggerFactory.getLogger(ObjectPos.class.getName());

	private int _objectId;
	private int _x;
	private int _y;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_x = readD();
		_y = readD();
	}

	@Override
	public void run()
	{
		_log.debug("ObjectPos " + _objectId + " " + _x + ", " + _y);
		ObjectCache.getInstance().getObject(_objectId).setCoord(_x, _y);
		ObjectCache.getInstance().getObject(_objectId).StopMove();
	}
}
