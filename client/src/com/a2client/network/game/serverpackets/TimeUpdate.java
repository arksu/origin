package com.a2client.network.game.serverpackets;

import com.a2client.network.game.GamePacketHandler;

public class TimeUpdate extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x0C, TimeUpdate.class);
	}

	int _minutes;
	int _temp;
	int _weather;

	@Override
	public void readImpl()
	{
		_minutes = readD();
		_temp = readC();
		_weather = readC();
	}

	@Override
	public void run()
	{

	}
}
