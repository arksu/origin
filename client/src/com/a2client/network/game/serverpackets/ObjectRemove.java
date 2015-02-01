package com.a2client.network.game.serverpackets;

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

    }
}
