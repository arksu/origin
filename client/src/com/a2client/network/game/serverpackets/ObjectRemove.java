package com.a2client.network.game.serverpackets;

import com.a2client.ObjectCache;

/**
 * Created by arksu on 02.02.15.
 */
public class ObjectRemove extends GameServerPacket {
    int _objectId;
    
    @Override
    public void readImpl() {
        _objectId = readD();
    }

    @Override
    public void run() {
        ObjectCache.getInstance().removeObject(_objectId);
    }
}
