package com.a2client.network.game.serverpackets;

import com.a2client.network.game.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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

	private int _parentObjectId;

	/**
	 * слоты с ид прилинкованных объектов
	 */
	private Map<Integer, Integer> _lift = new HashMap<>();

	@Override
	public void readImpl()
	{
		_parentObjectId = readD();
		int c = readC();
		while (c > 0)
		{
			c--;
			int slot = readC();
			int id = readD();
			_lift.put(slot, id);
		}
	}

	@Override
	public void run()
	{
		_log.debug("lift: " + _parentObjectId + " sz=" + _lift.size());
//		ObjectCache.getInstance().getObject(_parentObjectId)
	}
}
