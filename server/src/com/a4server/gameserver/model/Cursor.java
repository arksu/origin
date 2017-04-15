package com.a4server.gameserver.model;

import com.a4server.gameserver.network.serverpackets.CursorSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * представление курсора игрока
 * Created by arksu on 12.08.16.
 */
public class Cursor
{
	private static final Logger _log = LoggerFactory.getLogger(Cursor.class.getName());

	public enum CursorName
	{
		Arrow("arrow"),
		LiftUp("lift_up"),
		Spawn("spawn"),

		TileUp("tile_up"),
		TileDown("tile_down"),
		TileSand("tile_sand"),
		TileGrass("tile_grass");

		private final String _name;

		CursorName(String name)
		{
			_name = name;
		}
	}

	private CursorName _name;
	private int _typeId = 0;
	private final Player _player;

	public Cursor(Player player)
	{
		_name = CursorName.Arrow;
		_player = player;
	}

	public CursorName getName()
	{
		return _name;
	}

	public int getTypeId()
	{
		return _typeId;
	}

	public void set(CursorName current, int typeId)
	{
		if (_name != current || _typeId != typeId)
		{
			_name = current;
			_typeId = typeId;
			_player.getClient().sendPacket(new CursorSet(_name._name, typeId));
		}
	}
}
