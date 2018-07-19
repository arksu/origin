package com.a4server.gameserver.network.packets.clientpackets;

import com.a4server.gameserver.network.packets.GamePktClient;
import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.network.packets.serverpackets.CharInfo;
import com.a4server.gameserver.network.packets.serverpackets.MapGrid;
import com.a4server.gameserver.network.packets.serverpackets.TimeUpdate;
import com.a4server.gameserver.network.packets.serverpackets.WorldInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 08.01.2015.
 */
public class EnterWorld extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(EnterWorld.class.getName());

	@Override
	public void readImpl()
	{
	}

	@Override
	public void run()
	{
		GamePktClient client = getClient();

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
			int px = client.getActiveChar().getPos().getX();
			int py = client.getActiveChar().getPos().getY();
			for (Grid grid : client.getActiveChar().getGrids())
			{
				sendPacket(new MapGrid(grid, px, py));
			}

			// обновим список видимости. автоматически вышлет клиенту новые объекты
			cha.updateVisibleObjects(true);
		}
	}
}