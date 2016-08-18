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

	private CursorName _current;
	private final Player _player;

	public Cursor(Player player)
	{
		_current = CursorName.Arrow;
		_player = player;
	}

	public CursorName get()
	{
		return _current;
	}

	public void set(CursorName current)
	{
		if (_current != current)
		{
			_current = current;
			_player.getClient().sendPacket(new CursorSet(_current._name));
		}
	}
}
