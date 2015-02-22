package com.a4server.gameserver.network.serverpackets;

import com.a4server.gameserver.model.GameObject;

/**
 * Created by arksu on 01.02.15.
 */
public class ObjectAdd extends GameServerPacket
{
    GameObject _object;

    public ObjectAdd(GameObject object)
    {
        _object = object;
    }

    @Override
    protected void write()
    {
        if (_object.getObjectId() < 10)
        {
            System.out.println(
                    "ObjectAdd " + _object.getObjectId() + " " + _object.getPos()._x + ", " + _object.getPos()._y);
        }
        writeC(0x11);
        writeD(_object.getObjectId());
        writeD(_object.getTypeId());
        writeD(_object.getPos()._x);
        writeD(_object.getPos()._y);
        writeS(_object.getName());
        writeS(_object.getTitle());
    }
}
