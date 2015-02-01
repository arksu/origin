package com.a2client.network.game.serverpackets;

import com.a2client.Log;

/**
 * Created by arksu on 02.02.15.
 */
public class ObjectAdd extends GameServerPacket {
    int _objectId;
    int _typeId;
    int _x;
    int _y;
    
    @Override
    public void readImpl() {
        _objectId = readD();
        _typeId = readD();
        _x = readD();
        _y = readD();
                 
    }

    @Override
    public void run() {
        Log.info("ObjectAdd type="+_typeId);
    }
}
