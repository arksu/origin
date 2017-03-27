package com.a2client.network.game.serverpackets;

import com.a2client.PlayerData;
import com.a2client.network.game.GamePacketHandler;
import com.a2client.network.game.clientpackets.EnterWorld;
import com.a2client.screens.Game;

public class CharSelected extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x0A, CharSelected.class);
	}

	int _objectId;
	String _name;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_name = readS();
	}

	@Override
	public void run()
	{
		Game.Show();
		PlayerData.getInstance().setObjectId(_objectId);
		PlayerData.getInstance().setName(_name);
		Game.setStatusText(_name + " enter world...");

		sendPacket(new EnterWorld());
	}
}
