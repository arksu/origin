package com.a2client.network.game.serverpackets;

import com.a2client.screens.Game;

public class WorldInfo extends GameServerPacket
{
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
