package com.a2client.network.game.serverpackets;

import com.a2client.ObjectCache;
import com.a2client.network.game.GamePacketHandler;

/**
 * Created by arksu on 02.02.15.
 */
public class ObjectRemove extends GameServerPacket
{
    static
    {
        GamePacketHandler.AddPacketType(0x12, ObjectRemove.class);
    }

    int _objectId;

    @Override
    public void readImpl()
    {
        _objectId = readD();
    }

    @Override
    public void run()
    {
        ObjectCache.getInstance().removeObject(_objectId);
    }
}
