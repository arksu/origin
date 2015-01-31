package com.a2client.network.game.serverpackets;

import com.a2client.screens.CharacterSelect;

public class CharacterCreateFail extends GameServerPacket
{
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
