package com.a2client.network.game.serverpackets;

import com.a2client.model.Action;
import com.a2client.network.game.GamePacketHandler;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 13.10.15.
 */
public class Actions extends GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(Actions.class.getName());

	static
	{
		GamePacketHandler.AddPacketType(0x20, Actions.class);
	}

	private static Gson _gson = new Gson();

	Action[] _list;

	@Override
	public void readImpl()
	{
		String list = readS();
		_list = _gson.fromJson(list, Action[].class);
		_log.debug("recv actions: "+_list.length);
	}

	@Override
	public void run()
	{
	}
}
