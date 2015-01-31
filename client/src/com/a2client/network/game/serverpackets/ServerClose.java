package com.a2client.network.game.serverpackets;

import com.a2client.Main;
import com.a2client.screens.Login;

public class ServerClose extends GameServerPacket
{
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
