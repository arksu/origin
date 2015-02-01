package com.a2client.network.game.serverpackets;

/**
 * Created by arksu on 02.02.15.
 */
public class PlayerAppearance extends GameServerPacket {
    int _objectId;
    int _hairStyle;
    int _hairColor;
    int _face;
    boolean _isFemale;
    
    @Override
    public void readImpl() {
        _objectId = readD();
        _isFemale = readC() == 1;
        _hairStyle = readC();
        _hairColor = readC();
        _face = readC();
    }

    @Override
    public void run() {

    }
}
