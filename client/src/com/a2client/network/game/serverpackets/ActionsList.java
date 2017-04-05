package com.a2client.network.game.serverpackets;

import com.a2client.PlayerData;
import com.a2client.model.Action;
import com.a2client.network.game.GamePacketHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * список действий доступных игроку
 * Created by arksu on 13.10.15.
 */
public class ActionsList extends GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(ActionsList.class.getName());
	private static final Type listType = new TypeToken<ArrayList<Action>>() {}.getType();

	static
	{
		GamePacketHandler.AddPacketType(0x20, ActionsList.class);
	}

	private static Gson _gson = new Gson();

	private List<Action> _actions;

	@Override
	public void readImpl()
	{
		String list = readS();
		// cписок шлем в json
		_actions = _gson.fromJson(list, listType);
	}

	@Override
	public void run()
	{
		PlayerData.getInstance().setActions(_actions);
	}
}
