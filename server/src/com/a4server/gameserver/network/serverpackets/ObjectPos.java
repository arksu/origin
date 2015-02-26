package com.a4server.gameserver.network.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 11.02.15.
 */
public class ObjectPos extends GameServerPacket
{
    private static final Logger _log = LoggerFactory.getLogger(ObjectPos.class.getName());

    private int _objectId;
    private int _x;
    private int _y;

    public ObjectPos(int objectId, int x, int y)
    {
        _objectId = objectId;
        _x = x;
        _y = y;
    }

    @Override
    protected void write()
    {
        writeC(0x15);
        writeD(_objectId);
        writeD(_x);
        writeD(_y);
    }
}
