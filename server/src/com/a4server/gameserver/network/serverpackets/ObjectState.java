package com.a4server.gameserver.network.serverpackets;

/**
 * передаем инфу о состоянии объекта (состояние может изменятся)
 * Created by arksu on 09.10.15.
 */
public class ObjectState extends GameServerPacket
{
	private int _objectId;
	private String _jsonState;
	private byte[] _state;

	public ObjectState(int objectId, String jsonState)
	{
		_objectId = objectId;
		_jsonState = jsonState;
	}

	@Override
	protected void write()
	{
		writeC(0x1F);
		writeD(_objectId);

		writeS(_jsonState);

		writeC(_state.length);
		writeB(_state);
	}
}
