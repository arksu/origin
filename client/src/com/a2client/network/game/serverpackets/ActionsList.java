package com.a2client.network.game.serverpackets;

import com.a2client.Player;
import com.a2client.model.Action;
import com.a2client.network.game.GamePacketHandler;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * список действий доступных игроку
 * Created by arksu on 13.10.15.
 */
public class ActionsList extends GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ActionsList.class.getName());

	static
	{
		GamePacketHandler.AddPacketType(0x20, ActionsList.class);
	}

	private static Gson _gson = new Gson();

	@Override
	public void readImpl()
	{
		String list = readS();
		// cписок шлем в json
		Action[] list1 = _gson.fromJson(list, Action[].class);
		_log.debug("recv actions: "+ list1.length);
		Player.getInstance().setActionsList(list1);
	}

	@Override
	public void run()
	{
	}
}
