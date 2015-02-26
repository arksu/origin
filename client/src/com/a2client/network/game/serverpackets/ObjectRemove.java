package com.a2client.network.game.serverpackets;

import com.a2client.ObjectCache;
import com.a2client.network.game.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 02.02.15.
 */
public class ObjectRemove extends GameServerPacket
{
    static
    {
        GamePacketHandler.AddPacketType(0x12, ObjectRemove.class);
    }

    private static final Logger _log = LoggerFactory.getLogger(ObjectRemove.class.getName());

    int _objectId;

    @Override
    public void readImpl()
    {
        _objectId = readD();
    }

    @Override
    public void run()
    {
        _log.debug("ObjectRemove "+_objectId);
        ObjectCache.getInstance().removeObject(_objectId);
        // todo : при удалении объекта надо удалить все инвентари этого объекта
    }
}
