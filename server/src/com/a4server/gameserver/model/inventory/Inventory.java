package com.a4server.gameserver.model.inventory;

import com.a4server.Database;
import com.a4server.gameserver.model.GameObject;
import javolution.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * инвентарь объекта
 * Created by arksu on 24.02.15.
 */
public class Inventory
{
	private static final Logger _log = LoggerFactory.getLogger(Inventory.class.getName());

	public static final String LOAD_INVENTORY = "SELECT id, itemId, x, y, q, amount, stage, ticks, ticksTotal FROM items WHERE objectId=?";

	/**
	 * объект родитель
	 */
	private final GameObject _parent;

	/**
	 * ид инвентаря, объект или вещь к которой он относится
	 */
	private final int _invenroyId;

	/**
	 * инвентарь родитель
	 */
	private final Inventory _inventory;

	/**
	 * размеры
	 */
	private int _width;
	private int _height;

	/**
	 * список вещей которые находятся внутри
	 */
	FastList<InventoryItem> _items = new FastList<>();

	public Inventory(GameObject parent, int width, int height)
	{
		_parent = parent;
		_inventory = null;
		_invenroyId = parent.getObjectId();
		_width = width;
		_height = height;
		load();
	}

	public Inventory(Inventory parent, int objectId, int width, int height)
	{
		_inventory = parent;
		_invenroyId = objectId;
		_parent = null;
		_width = width;
		_height = height;
		load();
	}

	/**
	 * загрузить инвентарь из базы
	 */
	private void load()
	{
		_log.debug("load inventory: " + _invenroyId);
		try
		{
			try (Connection con = Database.getInstance().getConnection();
				 PreparedStatement ps = con.prepareStatement(LOAD_INVENTORY))
			{
				ps.setInt(1, _invenroyId);
				try (ResultSet rset = ps.executeQuery())
				{
					while (rset.next())
					{
						_items.add(new InventoryItem(this, rset));
					}
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn("Cant load inventory " + toString());
			throw new RuntimeException("Cant load inventory " + toString());
		}
	}

	public GameObject getParent()
	{
		return _parent;
	}

	public FastList<InventoryItem> getItems()
	{
		return _items;
	}

	public int getInvenroyId()
	{
		return _invenroyId;
	}

	public int getWidth()
	{
		return _width;
	}

	public int getHeight()
	{
		return _height;
	}
}
