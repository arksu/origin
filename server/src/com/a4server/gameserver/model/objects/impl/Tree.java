package com.a4server.gameserver.model.objects.impl;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.objects.ObjectTemplate;
import com.a4server.gameserver.model.objects.ObjectsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 22.08.16.
 */
public class Tree extends GameObject
{
	private static final Logger _log = LoggerFactory.getLogger(Tree.class.getName());

	public Tree(Grid grid, ResultSet rset) throws SQLException
	{
		super(grid, rset);
	}

	public Tree(int objectId, ObjectTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public List<String> getContextMenu(Player player)
	{
		List<String> list = new ArrayList<>();
		list.add("take_branch");
		list.add("chop");
		return list;
	}

	@Override
	protected void contextRun(Player player, String contextItem)
	{
		_log.debug("context: " + contextItem);

		if ("take_branch".equals(contextItem))
		{
			player.generateItem(ObjectsFactory.getInstance().getTemplate("branch").getTypeId(), getQuality());
		}
	}
}
