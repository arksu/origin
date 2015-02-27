package com.a4server.gameserver.model.ai.player;

import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 28.02.15.
 */
public class MindMoveAction extends PlayerMind
{
    private static final Logger _log = LoggerFactory.getLogger(MindMoveAction.class.getName());

    public MindMoveAction(Player player)
    {
        super(player);
    }

    @Override
    public void onArrived()
    {

    }

    @Override
    public void onTick()
    {

    }

    @Override
    public void handleEvent(Event event)
    {

    }
}
