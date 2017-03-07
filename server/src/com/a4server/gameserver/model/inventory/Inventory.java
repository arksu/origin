package com.a4server.gameserver.model.inventory;

import com.a4server.Database;
import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.objects.InventoryTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	public static final String LOAD_INVENTORY = "SELECT id, itemId, x, y, q, amount, stage, ticks, ticksTotal, del FROM items WHERE inventoryId=? AND x < 200 AND del=0";

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
	 * шаблон инвентаря
	 */
	protected InventoryTemplate _template;

	/**
	 * список вещей которые находятся внутри
	 */
	protected final Map<Integer, InventoryItem> _items = new ConcurrentHashMap<>();

	/**
	 * это инвентарь непосредственно объекта
	 */
	public Inventory(GameObject object, int width, int height)
	{
		_object = object;
		_invenroyId = object.getObjectId();
		_width = width;
		_height = height;
		_template = object.getTemplate().getInventory();
		if (_template == null)
		{
			_log.error("inventory does not have template!");
		}
		load();
	}

	/**
	 * это вложенный инвентарь
	 */
	public Inventory(GameObject object, int objectId, InventoryTemplate template)
	{
		_object = object;
		_invenroyId = objectId;
		_template = template;
		_width = _template.getWidth();
		_height = _template.getHeight();
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
						InventoryItem item = new InventoryItem(this, rset);
						_items.put(item.getObjectId(), item);
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

	public Map<Integer, InventoryItem> getItems()
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
		// пытаемся найти вещь по ид
		InventoryItem item = _items.get(objectId);
		if (item != null)
		{
			return item;
		}
		// в этом инвентаре указанной вещи нет, ищем во вложенных
		for (InventoryItem inv : _items.values())
		{
			if (inv.getInventory() != null)
			{
				InventoryItem found = inv.getInventory().findItem(objectId);
				if (found != null)
				{
					return found;
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

		for (InventoryItem item : _items.values())
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
	public InventoryItem takeItem(InventoryItem item)
	{
		return _items.remove(item.getObjectId());
	}

	/**
	 * положить вещь в инвентарь. ищем свободное место под нее с учетом ее размеров
	 * @param itemPut ведь которую кладем
	 * @return {@link InventoryItem} положенная вещь если удалось найти под нее место, иначе null
	 */
	public InventoryItem putItem(AbstractItem itemPut)
	{
		return putItem(itemPut, -1, -1);
	}

	/**
	 * положить вещь в инвентарь
	 * @param x координаты куда положить вещь в инвентаре
	 * @param y координаты куда положить вещь в инвентаре
	 * @return положенная вещь если удалось найти под нее место, иначе null
	 */
	public InventoryItem putItem(AbstractItem itemPut, int x, int y)
	{
		// проверить можем ли мы вообще положить такую вещь в этот инвентарь?
		if (!_template.isAccept(itemPut)) return null;

		// приведем к типу
		InventoryItem item = itemPut instanceof InventoryItem ? ((InventoryItem) itemPut) : new InventoryItem(itemPut);

		// знаем куда положить?
		if (x >= 0 && y >= 0)
		{
			return tryPut(item, x, y) ? item : null;
		}
		else
		{
			// мы не знаем куда положить. ищем свободное место
			// перебираем все доступные координаты в инвентаре
			for (int iy = 0; iy < _height; iy++)
			{
				for (int ix = 0; ix < _width; ix++)
				{
					if (tryPut(item, ix, iy)) return item;
				}
			}
		}
		return null;
	}

	/**
	 * попробовать положить вещь в указанное место
	 * @return истину если положить удалось, сразу же обновляем инвентарь и вещь
	 */
	protected boolean tryPut(InventoryItem item, int x, int y)
	{
		synchronized (this)
		{
			// если вещь влезает в инвентарь
			if (x + item.getWidth() <= getWidth() && y + item.getHeight() <= getHeight())
			{
				boolean conflict = false;
				// пройдемся по всем вещам в инвентаре
				for (InventoryItem i : _items.values())
				{
					// убедимся что новая вещь не пересекается ни с одной внутри
					if (i.contains(x, y, item.getWidth(), item.getHeight()))
					{
						conflict = true;
						break;
					}
				}
				if (!conflict)
				{
					_items.put(item.getObjectId(), item);
					item.setParentInventory(this);
					item.setXY(x, y);
					return true;
				}
			}
		}
		return false;
	}
}
