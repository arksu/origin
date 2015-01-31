package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.GameClient;
import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.network.serverpackets.Init;
import com.a4server.gameserver.network.serverpackets.MapGrid;
import com.a4server.gameserver.network.serverpackets.TimeUpdate;
import com.a4server.gameserver.network.serverpackets.WorldInfo;
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
        // начинаем слать клиенту игровую информацию
        sendPacket(new WorldInfo().addNext(new TimeUpdate().addNext(new Init())));

        // сначала гриды
        int px = client.getActiveChar().getPos()._x;
        int py = client.getActiveChar().getPos()._y;
        for (Grid grid : client.getActiveChar().getGrids())
        {
            sendPacket(new MapGrid(grid, px, py));
        }

    }
}