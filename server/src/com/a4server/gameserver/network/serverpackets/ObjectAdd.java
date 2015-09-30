package com.a4server.gameserver.network.serverpackets;

import com.a4server.gameserver.model.GameObject;

/**
 * Created by arksu on 01.02.15.
 */
public class ObjectAdd extends GameServerPacket
{
	GameObject _object;

	public ObjectAdd(GameObject object)
	{
		_object = object;
	}

	@Override
	protected void write()
	{
		writeC(0x11);
		writeD(_object.getObjectId());
		writeD(_object.getTypeId());
		writeD(_object.getPos()._x);
		writeD(_object.getPos()._y);
		writeS(_object.getName());
		writeS(_object.getTitle());
	}
}
