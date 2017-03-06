package com.a4server.gameserver.model.ai.player;

import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.ai.AI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * абстракция мозга для игрока
 * Created by arksu on 28.02.15.
 */
public abstract class PlayerAI implements AI
{
	private static final Logger _log = LoggerFactory.getLogger(PlayerAI.class.getName());

	protected final Player _player;

	public PlayerAI(Player player)
	{
		_player = player;
	}

	public void dispose()
	{
	}
}
