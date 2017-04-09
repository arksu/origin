package com.a4server.gameserver.model.objects.impl;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Grid;
import com.a4server.gameserver.model.objects.ObjectTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by arksu on 09.04.17.
 */
public class Stump extends GameObject
{
	public Stump(int objectId, ObjectTemplate template)
	{
		super(objectId, template);
	}

	public Stump(Grid grid, ResultSet rset) throws SQLException
	{
		super(grid, rset);
	}
}
