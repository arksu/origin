package com.a2client.network.game.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * клик по карте
 * Created by arksu on 08.02.15.
 */
public class MouseClick extends GameClientPacket
{
    protected static final Logger _log = LoggerFactory.getLogger(MouseClick.class.getName());

    private final int _button;
    private final int _x;
    private final int _y;
    private final int _objectId;

    public MouseClick(boolean isDown, int button, int x, int y, int objectId)
    {
        // если кнопку подняли - то добавим 10
        _button = button + (isDown ? 0 : 10);
        _x = x;
        _y = y;
        _objectId = objectId;
    }

    @Override
    protected void write()
    {
        writeC(0x0E);
        writeC(_button);
        writeD(_x);
        writeD(_y);
        writeD(_objectId);
    }
}
