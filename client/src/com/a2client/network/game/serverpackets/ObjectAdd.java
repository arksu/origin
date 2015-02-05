package com.a2client.network.game.serverpackets;

import com.a2client.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 02.02.15.
 */
public class ObjectAdd extends GameServerPacket {
    private static final Logger _log = LoggerFactory.getLogger(ObjectAdd.class.getName());

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
        _log.debug("ObjectAdd " + _objectId + " type=" + _typeId);
    }
}
