package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.GameClient;
import com.a4server.gameserver.model.Grid;
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
        _log.info("EnterWorld");
        GameClient client = getClient();

        // ПОЕХАЛИ!
        // только если у нас есть активный чар
        if (client.getActiveChar() != null)
        {
            // начинаем слать клиенту игровую информацию
            sendPacket(new WorldInfo());
            sendPacket(new TimeUpdate());
            sendPacket(new CharInfo(getClient().getActiveChar()));

            // сначала гриды
            int px = client.getActiveChar().getPos()._x;
            int py = client.getActiveChar().getPos()._y;
            for (Grid grid : client.getActiveChar().getGrids())
            {
                sendPacket(new MapGrid(grid, px, py));
            }
            
            // обновим список видимости. автоматически вышлет клиенту новые объекты
            client.getActiveChar().UpdateVisibleObjects(true);
        }
    }
}