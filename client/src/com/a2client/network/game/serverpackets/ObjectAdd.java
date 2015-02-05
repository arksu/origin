package com.a2client.network.game.serverpackets;

import com.a2client.ObjectCache;
import com.a2client.model.GameObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 02.02.15.
 */
public class ObjectAdd extends GameServerPacket
{
    private static final Logger _log = LoggerFactory.getLogger(ObjectAdd.class.getName());

    public int _objectId;
    public int _typeId;
    public int _x;
    public int _y;
    public String _name;
    public String _title;

    @Override
    public void readImpl()
    {
        _objectId = readD();
        _typeId = readD();
        _x = readD();
        _y = readD();
        _name = readS();
        _title = readS();
    }

    @Override
    public void run()
    {
        _log.debug("ObjectAdd " + _objectId + " type=" + _typeId);
        ObjectCache.getInstance().addObject(new GameObject(this));
    }
}
