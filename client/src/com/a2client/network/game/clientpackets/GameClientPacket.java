package com.a2client.network.game.clientpackets;

import com.a2client.network.Net;
import com.a2client.util.network.BaseSendPacket;

public abstract class GameClientPacket extends BaseSendPacket
{
    public void Send()
    {
        Net.getConnection().sendPacket(this);
    }
}
