package com.a2client.network.game.serverpackets;

import com.a2client.network.game.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 26.04.17.
 */
public class ObjectLift extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x26, ObjectLift.class);
	}

	private static final Logger _log = LoggerFactory.getLogger(ObjectLift.class.getName());

	private int _liftedObjectId;
	private int _parentObjectId;

	@Override
	public void readImpl()
	{
		_liftedObjectId = readD();
		_parentObjectId = readD();
	}

	@Override
	public void run()
	{
		_log.debug("lift: " + _liftedObjectId + " -> " + _parentObjectId);
//		ObjectCache.getInstance().getObject(_parentObjectId)
	}
}
