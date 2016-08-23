package com.a4server.gameserver.model.objects.impl;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 24.08.16.
 */
public class Stone extends GameObject
{
	private static final Logger _log = LoggerFactory.getLogger(Stone.class.getName());

	public Stone(int objectId, ObjectTemplate template)
	{
		super(objectId, template);
	}

	public Stone(Grid grid, ResultSet rset) throws SQLException
	{
		super(grid, rset);
	}

	@Override
	public List<String> getContextMenu(Player player)
	{
		List<String> list = new ArrayList<>();
		list.add("chip_stone");
		return list;
	}

	@Override
	public void contextSelected(Player player, String item)
	{

	}
}
