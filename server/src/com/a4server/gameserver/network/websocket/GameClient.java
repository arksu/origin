package com.a4server.gameserver.network.websocket;

import com.a4server.gameserver.model.Player;

public class GameClient
{
	private Player _player;

	private State _state = State.CONNECTED;

	public enum State
	{
		CONNECTED
	}

	public State getState()
	{
		return _state;
	}

	public Player getPlayer()
	{
		return _player;
	}

	public void setPlayer(Player player)
	{
		_player = player;
	}
}
