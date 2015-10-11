package com.a2client.network.game.serverpackets;

import com.a2client.network.game.GamePacketHandler;

/**
 * Created by arksu on 12.10.15.
 */
public class ObjectState extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x1F, ObjectState.class);
	}

	private int _objectId;
	private String _jsonState;
	private byte[] _state;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_jsonState = readS();
		int len = readC();
		_state = readB(len);
	}

	@Override
	public void run()
	{

	}
}
