package com.a4server.gameserver.model.ai.player;

import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.ai.Mind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 28.02.15.
 */
public abstract class PlayerMind implements Mind
{
    private static final Logger _log = LoggerFactory.getLogger(PlayerMind.class.getName());

    protected Player _player;

    public PlayerMind(Player player)
    {
        _player = player;
    }
}
