package com.a4server.gameserver.model;

import com.a4server.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * эквип / папердолл
 * Created by arksu on 12.09.15.
 */
public class Equip
{
	private static final Logger _log = LoggerFactory.getLogger(Equip.class.getName());

	/**
	 * грузим эквип из основной таблицы с вещами, x=200
	 * y - определяет номер слота в котором находится вещь
	 */
	public static final String LOAD_EQUIP = "SELECT id, itemId, x, y, q, amount, stage, ticks, ticksTotal FROM items WHERE objectId=? AND x = 200";

	final Player _player;

	/**
	 * ид игрока
	 */
	final int _objectId;

	final Map<EquipSlot.Slot, EquipSlot> _items = new HashMap<>();

	public Equip(Player player)
	{
		_player = player;
		_objectId = player.getObjectId();
		load();
	}

	private void load()
	{
		_log.debug("load equip: " + _objectId);
		try
		{
			try (Connection con = Database.getInstance().getConnection();
				 PreparedStatement ps = con.prepareStatement(LOAD_EQUIP))
			{
				ps.setInt(1, _objectId);
				try (ResultSet rset = ps.executeQuery())
				{
					while (rset.next())
					{
						EquipSlot slot = new EquipSlot(_player, rset);
						_items.put(slot.getSlot(), slot);
					}
					_log.debug("loaded " + _items.size() + " items");
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn("Cant load equip " + toString());
			throw new RuntimeException("Cant load equip " + toString());
		}
	}
}
