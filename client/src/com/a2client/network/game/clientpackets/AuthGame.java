package com.a2client.network.game.clientpackets;

import com.a2client.screens.Login;

public class AuthGame extends GameClientPacket
{
	public AuthGame()
	{
	}

	@Override
	protected void write()
	{
		writeC(0x02);
		// шлем ид сессии полученный от логин сервера
		writeD(Login._gameserver_key1);
		writeD(Login._gameserver_key2);
	}
}
