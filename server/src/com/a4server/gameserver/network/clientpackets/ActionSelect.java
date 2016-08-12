package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.model.Cursor;
import com.a4server.gameserver.model.GameLock;
import com.a4server.gameserver.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a4server.gameserver.model.Cursor.CursorName.*;

/**
 * игрок выбрал некое действие
 * Created by arksu on 18.10.15.
 */
public class ActionSelect extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ActionSelect.class.getName());

	private String _name;

	@Override
	public void readImpl()
	{
		_name = readS();
	}

	@Override
	public void run()
	{
		_log.debug("action: " + _name);

		Player player = client.getPlayer();
		if (player != null)
		{
			try (GameLock ignored = player.tryLock())
			{
				Cursor.CursorName cursor = Arrow;
				if ("online".equals(_name))
				{

				}
				else if ("tile_up".equals(_name))
				{
					cursor = TileUp;
				}
				else if ("tile_down".equals(_name))
				{
					cursor = TileDown;
				}
				player.setCursor(cursor);
			}
			catch (Exception e)
			{
				_log.error("ActionSelect error: "+e.getMessage(), e);
			}
		}
	}
}
