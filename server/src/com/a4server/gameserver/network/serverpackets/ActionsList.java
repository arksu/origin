package com.a4server.gameserver.network.serverpackets;

import com.a4server.gameserver.model.Action;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by arksu on 13.10.15.
 */
public class ActionsList extends GameServerPacket
{
	private static Gson _gson = new GsonBuilder().create();

	/**
	 * список действий в json
	 */
	private final Action[] _actions;

	public ActionsList(Action[] actions)
	{
		_actions = actions;
	}

	@Override
	protected void write()
	{
		writeC(0x20);
		writeS(_gson.toJson(_actions, Action[].class));
	}
}
