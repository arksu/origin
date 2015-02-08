package com.a4server.gameserver.network.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 08.02.15.
 */
public class MouseClick extends GameClientPacket
{
    protected static final Logger _log = LoggerFactory.getLogger(MouseClick.class.getName());

    private int _button;
    private int _x;
    private int _y;
    private int _objectId;

    @Override
    public void readImpl()
    {
        _button = readC();
        _x = readD();
        _y = readD();
        _objectId = readD();
    }

    @Override
    public void run()
    {

    }
}
