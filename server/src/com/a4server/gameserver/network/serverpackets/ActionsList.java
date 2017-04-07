package com.a4server.gameserver.network.serverpackets;

import com.a4server.gameserver.model.ActionItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 13.10.15.
 */
public class ActionsList extends GameServerPacket
{
	private static final Gson _gson = new GsonBuilder().create();
	private static final Type listType = new TypeToken<ArrayList<ActionItem>>() {}.getType();

	/**
	 * список действий в json
	 */
	private final List<ActionItem> _actionItems;

	public ActionsList(List<ActionItem> actionItems)
	{
		_actionItems = actionItems;
	}

	@Override
	protected void write()
	{
		writeC(0x20);
		writeS(_gson.toJson(_actionItems, listType));
	}
}
