package com.a2client.network.game.serverpackets;

import com.a2client.Log;
import com.a2client.Player;
import com.a2client.network.game.clientpackets.EnterWorld;
import com.a2client.screens.Game;

public class CharSelected extends GameServerPacket
{
    int _objectId;
    String _name;

    @Override
    public void readImpl()
    {
        _objectId = readD();
        _name = readS();
    }

    @Override
    public void run()
    {
        Game.Show();
        Player.getInstance().setObjectId(_objectId);
        Player.getInstance().setName(_name);
        Game.setStatusText(_name + " enter world...");

        sendPacket(new EnterWorld());
    }
}
