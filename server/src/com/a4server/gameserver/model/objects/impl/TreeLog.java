package com.a4server.gameserver.model.objects.impl;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.objects.ObjectTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 09.04.17.
 */
public class TreeLog extends GameObject
{
	public TreeLog(Grid grid, ResultSet rset) throws SQLException
	{
		super(grid, rset);
	}

	public TreeLog(int objectId, ObjectTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public List<String> getContextMenu(Player player)
	{
		List<String> list = new ArrayList<>();
		list.add("make_boards");
		return list;
	}

}
