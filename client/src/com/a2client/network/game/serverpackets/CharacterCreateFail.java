package com.a2client.network.game.serverpackets;

import com.a2client.network.game.GamePacketHandler;
import com.a2client.screens.CharacterSelect;

public class CharacterCreateFail extends GameServerPacket
{
    static
    {
        GamePacketHandler.AddPacketType(0x07, CharacterCreateFail.class);
    }

    int _reason;

    @Override
    public void readImpl()
    {
        _reason = readC();
    }

    @Override
    public void run()
    {
        switch (_reason)
        {
            default:
                CharacterSelect.Show();
                CharacterSelect.setStatusText("Character create failed: unknown reason " + _reason);
        }
    }
}
