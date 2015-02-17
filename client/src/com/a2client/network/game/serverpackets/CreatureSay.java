package com.a2client.network.game.serverpackets;

import com.a2client.network.game.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 17.02.15.
 */
public class CreatureSay extends GameServerPacket
{
    static
    {
        GamePacketHandler.AddPacketType(0x17, CreatureSay.class);
    }

    private static final Logger _log = LoggerFactory.getLogger(CreatureSay.class.getName());

    private int _objectId;
    private String _message;

    @Override
    public void readImpl()
    {
        _objectId = readD();
        _message = readS();
    }

    @Override
    public void run()
    {
        _log.debug("CreatureSay: " + _objectId + ": " + _message);
    }
}
