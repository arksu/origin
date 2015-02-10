package com.a4server.gameserver.network.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * объект двигается
 * Created by arksu on 11.02.15.
 */
public class ObjectMove extends GameServerPacket
{
    protected static final Logger _log = LoggerFactory.getLogger(ObjectMove.class.getName());

    private int _objectId;
    private int _tox;
    private int _toy;
    private int _vx;
    private int _vy;

    public ObjectMove(int objectId, int tox, int toy, int vx, int vy)
    {
        _objectId = objectId;
        _tox = tox;
        _toy = toy;
        _vx = vx;
        _vy = vy;
    }

    @Override
    protected void write()
    {
        writeC(0x14);
        writeD(_objectId);
        writeD(_tox);
        writeD(_toy);
        writeD(_vx);
        writeD(_vy);
    }
}
