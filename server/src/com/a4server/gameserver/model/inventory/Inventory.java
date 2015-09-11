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
	private final GameObject _object;

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

	public Inventory(GameObject object, int width, int height)
	{
		_object = object;
		_inventory = null;
		_invenroyId = object.getObjectId();
		_width = width;
		_height = height;
		load();
	}

	public Inventory(Inventory parent, int objectId, int width, int height)
	{
		_object = parent.getObject();
		_inventory = parent;
		_invenroyId = objectId;
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

	public GameObject getObject()
	{
		return _object;
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

	/**
	 * найти вещь в инвентаре и во всех его дочерних
	 */
	public InventoryItem findItem(int objectId)
	{
		for (InventoryItem item : _items)
		{
			if (item.getObjectId() == objectId)
			{
				return item;
			}
			if (item.getInventory() != null)
			{
				InventoryItem i = item.getInventory().findItem(objectId);
				if (i != null)
				{
					return i;
				}
			}
		}
		return null;
	}

	/**
	 * найти инвентарь внутри с указанны ид
	 */
	public Inventory findInventory(int invenroyId)
	{
		if (_invenroyId == invenroyId) return this;
		for (InventoryItem item : _items)
		{
			if (item.getInventory() != null)
			{
				Inventory i = item.getInventory().findInventory(invenroyId);
				if (i != null)
				{
					return i;
				}
			}
		}
		return null;
	}

	/**
	 * взять вещь из инвентаря
	 */
	public boolean takeItem(InventoryItem item)
	{
		return _items.remove(item);
	}

	/**
	 * положить вещь в инвентарь
	 * @param item вещь
	 * @param x координаты куда положить вещь в инвентаре
	 * @param y координаты куда положить вещь в инвентаре
	 */
	public void putItem(InventoryItem item, int x, int y)
	{

	}
}
