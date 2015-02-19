package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.model.Player;
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
        // нажатая кнопка < 10
        boolean isDown = _button < 10;
        _button = _button >= 10 ? _button - 10 : _button;
        Player player = client.getActiveChar();
        if (player != null)
        {
            if (isDown)
            {
                switch (_button)
                {
                    // LEFT
                    case 0 :
                        // если кликнули не в объект
                        if (_objectId == 0) {
                            player.MoveToPoint(_x, _y);
                        }
                }
            }
        }
    }
}
