package com.a2client.network.game.serverpackets;

import com.a2client.ObjectCache;
import com.a2client.network.game.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 11.02.15.
 */
public class ObjectMove extends GameServerPacket
{
    static
    {
        GamePacketHandler.AddPacketType(0x14, ObjectMove.class);
    }

    protected static final Logger _log = LoggerFactory.getLogger(ObjectMove.class.getName());

    private int _objectId;
    private int _tox;
    private int _toy;
    private int _vx;
    private int _vy;
    private int _speed;

    @Override
    public void readImpl()
    {
        _objectId = readD();
        _tox = readD();
        _toy = readD();
        _vx = readD();
        _vy = readD();
        _speed = readH();
    }

    @Override
    public void run()
    {
        _log.debug("ObjectMove " + _objectId + " " + _tox + ", " + _toy+" to >> "+_vx+", "+_vy+" speed="+_speed);
//        ObjectCache.getInstance().getObject(_objectId).setCoord(_tox, _toy);
        ObjectCache.getInstance().getObject(_objectId).Move(_tox, _toy, _vx, _vy, _speed);
    }
}
