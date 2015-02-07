package com.a2client.network.game.serverpackets;

import com.a2client.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharInfo extends GameServerPacket
{
    private static final Logger _log = LoggerFactory.getLogger(CharInfo.class.getName());

    private int _objectId;
    private String _name;

    @Override
    public void readImpl()
    {
        _objectId = readD();
        _name = readS();
        _log.debug("CharInfo: " + _name + " id=" + _objectId);
    }

    @Override
    public void run()
    {
        Player.getInstance().setObjectId(_objectId);
        Player.getInstance().setName(_name);
    }
}
