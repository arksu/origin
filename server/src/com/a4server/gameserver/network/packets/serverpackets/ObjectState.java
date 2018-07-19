package com.a4server.gameserver.network.packets.serverpackets;

/**
 * передаем инфу о состоянии объекта (состояние может изменятся)
 * Created by arksu on 09.10.15.
 */
public class ObjectState extends GameServerPacket
{
	private int _objectId;

	/**
	 * можем передать состояние как в виде json (для гибкости)
	 */
	private String _jsonState;

	/**
	 * или в виде бинарных данных (компактнее)
	 */
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
