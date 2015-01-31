package com.a2client.network.game.clientpackets;

public class MoveBackwardToLocation extends GameClientPacket
{
    int _originX, _originY, _targetX, _targetY;

    public MoveBackwardToLocation(int fromX, int fromY, int toX, int toY)
    {
        _originX = fromX;
        _originY = fromY;
        _targetX = toX;
        _targetY = toY;
    }

    @Override
    protected void write()
    {
        writeC(0x0E);

        writeD(_originX);
        writeD(_originY);
        writeD(_targetX);
        writeD(_targetY);
    }
}
