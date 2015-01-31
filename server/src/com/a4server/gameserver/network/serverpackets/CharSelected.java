package com.a4server.gameserver.network.serverpackets;

import com.a4server.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 05.01.2015.
 */
public class CharSelected extends GameServerPacket
{
    protected static final Logger _log = LoggerFactory.getLogger(CharSelected.class.getName());

    private Player _player;

    public CharSelected(Player player)
    {
        _player = player;
    }

    @Override
    protected void write()
    {
        writeC(0x0A);

        writeD(_player.getObjectId());
        writeS(_player.getName());
    }
}
