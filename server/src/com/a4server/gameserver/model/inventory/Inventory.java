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

	/**
	 * грузим список вещей из базы где строго x < 200, начиная с x>=200 храним особые
	 */
	public static final String LOAD_INVENTORY = "SELECT id, itemId, x, y, q, amount, stage, ticks, ticksTotal FROM items WHERE objectId=? AND x < 200";

	/**
	 * объект родитель, к которому относится инвентарь и все его вложенные
	 */
	private final GameObject _object;

	/**
	 * ид инвентаря, объект или вещь к которой он относится
	 */
	private final int _invenroyId;

	/**
	 * размеры
	 */
	private int _width;
	private int _height;

	/**
	 * список вещей которые находятся внутри
	 */
	FastList<InventoryItem> _items = new FastList<>();

	/**
	 * это инвентарь непосредственно объекта
	 * @param object
	 * @param width
	 * @param height
	 */
	public Inventory(GameObject object, int width, int height)
	{
		_object = object;
		_invenroyId = object.getObjectId();
		_width = width;
		_height = height;
		load();
	}

	/**
	 * это вложенный инвентарь
	 * @param object
	 * @param objectId
	 * @param width
	 * @param height
	 */
	public Inventory(GameObject object, int objectId, int width, int height)
	{
		_object = object;
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
					_log.debug("loaded " + _items.size() + " items");
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
	public boolean putItem(InventoryItem item, int x, int y)
	{
		// todo проверить можем ли мы вообще положить такую вещь в этот инвентарь?
		if (x >= 0 && y >= 0)
		{
			// если вещь влезает в инвентарь
			if (x + item.getWidth() <= getWidth() && y + item.getHeight() <= getHeight())
			{
				boolean conflict = false;
				for (InventoryItem i : _items)
				{
					if (i.contains(x, y, item.getWidth(), item.getHeight()))
					{
						conflict = true;
					}
				}
				if (!conflict)
				{
					_items.add(item);
					item.setXY(x, y);
					return true;
				}
			}
		}
		else
		{
			// todo мы не знаем куда положить. ищем свободное место. иначе вернем ложь

		}
		return false;
	}
}
