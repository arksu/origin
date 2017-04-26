package com.a4server.gameserver.network.serverpackets;

import com.a4server.gameserver.model.GameObject;

import java.util.Map;

/**
 * Created by arksu on 26.04.17.
 */
public class ObjectLift extends GameServerPacket
{
	private GameObject _object;

	public ObjectLift(GameObject object)
	{
		_object = object;
	}

	@Override
	protected void write()
	{
		writeC(0x26);
		writeD(_object.getObjectId());
		Map<Integer, GameObject> lift = _object.getLift();
		writeC(lift.size());
		for (Map.Entry<Integer, GameObject> entry : lift.entrySet())
		{
			writeC(entry.getKey());
			writeD(entry.getValue().getObjectId());
		}
	}
}
