package com.a4server.gameserver.network.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * передаем инфу о состоянии объекта (состояние может изменятся)
 * Created by arksu on 09.10.15.
 */
public class ObjectState extends GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ObjectState.class.getName());

	int _objectId;
	String _jsonState;

	@Override
	protected void write()
	{
		writeC(0x1F);
		writeD(_objectId);
		writeS(_jsonState);
	}
}
