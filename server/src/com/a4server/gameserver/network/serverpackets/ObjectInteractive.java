package com.a4server.gameserver.network.serverpackets;

/**
 * взаимодействие объекта с другими (включаем и выключаем)
 * Created by arksu on 28.02.15.
 */
public class ObjectInteractive extends GameServerPacket
{
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
