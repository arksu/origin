package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.GameClient;
import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.network.serverpackets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 08.01.2015.
 */
public class EnterWorld extends GameClientPacket
{
    protected static final Logger _log = LoggerFactory.getLogger(EnterWorld.class.getName());

    @Override
    public void readImpl()
    {

    }

    @Override
    public void run()
    {
        GameClient client = getClient();

        // ПОЕХАЛИ!
        Player cha = getClient().getActiveChar();
        // только если у нас есть активный чар
        if (cha != null)
        {
            _log.debug("EnterWorld " + cha.toString());
            // начинаем слать клиенту игровую информацию
            sendPacket(new WorldInfo());
            sendPacket(new TimeUpdate());
            sendPacket(new CharInfo(cha));

            // сначала гриды
            int px = client.getActiveChar().getPos()._x;
            int py = client.getActiveChar().getPos()._y;
            for (Grid grid : client.getActiveChar().getGrids())
            {
                sendPacket(new MapGrid(grid, px, py));
            }

            // обновим список видимости. автоматически вышлет клиенту новые объекты
            cha.UpdateVisibleObjects(true);
        }
    }
}