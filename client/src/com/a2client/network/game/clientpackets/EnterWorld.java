package com.a2client.network.game.clientpackets;

public class EnterWorld extends GameClientPacket
{
    @Override
    protected void write()
    {
        writeC(0x09);
    }
}
