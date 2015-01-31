package com.a2client.network.game.serverpackets;

import com.a2client.network.game.clientpackets.AuthGame;
import com.a2client.screens.Login;

public class Init extends GameServerPacket
{
    private int _proto_version;

    @Override
    public void readImpl()
    {
        _proto_version = readC();
    }

    @Override
    public void run()
    {
        if (_proto_version == 3)
        {
            new AuthGame().Send();
        }
        else
        {
            Login.Error("proto_version");
        }
    }
}
