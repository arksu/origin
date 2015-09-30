package com.a2client.network.game.serverpackets;

import com.a2client.network.game.GamePacketHandler;
import com.a2client.screens.Game;

public class WorldInfo extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x10, WorldInfo.class);
	}

	@Override
	public void readImpl()
	{
		Game.setStatusText("");
		Game.getInstance().setState(Game.GameState.IN_GAME);
	}

	@Override
	public void run()
	{

	}
}
