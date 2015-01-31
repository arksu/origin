package com.a2client.network.game.serverpackets;

public class TimeUpdate extends GameServerPacket
{
    int _minutes;
    int _temp;
    int _weather;

    @Override
    public void readImpl()
    {
        _minutes = readD();
        _temp = readC();
        _weather = readC();
    }

    @Override
    public void run()
    {

    }
}
