package com.a2client.network.game.serverpackets;

import com.a2client.Main;
import com.a2client.network.game.GamePacketHandler;
import com.a2client.screens.Login;

public class ServerClose extends GameServerPacket
{
    static
    {
        GamePacketHandler.AddPacketType(0x08, ServerClose.class);
    }

    @Override
    public void readImpl()
    {
    }

    @Override
    public void run()
    {
        Main.ReleaseAll();
        Login.setStatus("disconnected");
    }
}
