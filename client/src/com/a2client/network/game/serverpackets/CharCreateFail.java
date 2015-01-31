package com.a2client.network.game.serverpackets;

public class CharCreateFail extends GameServerPacket
{
    private int _reason;

    @Override
    public void readImpl()
    {
        _reason = readC();
    }

    @Override
    public void run()
    {

    }
}
