package com.a4server.gameserver.model.inventory;

import com.a4server.gameserver.model.objects.InventoryTemplate;
import com.a4server.gameserver.model.objects.ItemTemplate;
import com.a4server.gameserver.model.objects.ObjectsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * вещь в инвентаре
 * Created by arksu on 23.02.15.
 */
public class InventoryItem
{
	private static final Logger _log = LoggerFactory.getLogger(InventoryItem.class.getName());

	/**
	 * инвентарь в котором хранится данная вещь
	 */
	private Inventory _parentInventory;

	/**
	 * вещь также может содержать инвентарь (вложенный)
	 */
	private Inventory _inventory;

	/**
	 * уникальный ид объекта (вещи)
	 */
	private int _objectId;

	/**
	 * шаблон вещи
	 */
	private ItemTemplate _template;

	/**
	 * качество
	 */
	private int _q;

	/**
	 * место расположения
	 */
	private int _x;
	private int _y;

	private int _amount;
	private int _stage;
	private int _ticks;
	private int _ticksTotal;

	public InventoryItem(Inventory parentInventory, ResultSet rset)
	{
		_parentInventory = parentInventory;
		try
		{
			_objectId = rset.getInt("id");
			_template = ObjectsFactory.getInstance().getItemTemplate(rset.getInt("itemId"));
			_q = rset.getInt("q");
			_x = rset.getInt("x");
			_y = rset.getInt("y");
			_amount = rset.getInt("amount");
			_stage = rset.getInt("stage");
			_ticks = rset.getInt("ticks");
			_ticksTotal = rset.getInt("ticksTotal");

			// вещь тоже может содержать внутри себя инвентарь
			InventoryTemplate template = _template.getInventory();
			if (template != null)
			{
				_inventory = new Inventory(_parentInventory, _objectId, template.getWidth(), template.getHeight());
			}
		}
		catch (SQLException e)
		{
			_log.error("failed load item " + e.getMessage());
		}
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public Inventory getParentInventory()
	{
		return _parentInventory;
	}

	public int getQ()
	{
		return _q;
	}

	public int getX()
	{
		return _x;
	}

	public int getY()
	{
		return _y;
	}

	public void setXY(int x, int y)
	{
		_x = x;
		_y = y;
	}

	public int getWidth()
	{
		return _template.getWidth();
	}

	public int getHeight()
	{
		return _template.getHeight();
	}

	public int getAmount()
	{
		return _amount;
	}

	public int getStage()
	{
		return _stage;
	}

	public int getTicks()
	{
		return _ticks;
	}

	public int getTicksTotal()
	{
		return _ticksTotal;
	}

	public ItemTemplate getTemplate()
	{
		return _template;
	}

	public Inventory getInventory()
	{
		return _inventory;
	}

	public boolean contains(int x, int y, int w, int h)
	{
		final int tr = _x + _template.getWidth() - 1;
		final int tb = _y + _template.getHeight() - 1;
		final int r = x + w - 1;
		final int b = y + h - 1;
		return (
				((_x >= x) && (_x <= r)) || ((tr >= x) && (tr <= r)) ||
						((x >= _x) && (x <= tr)) || ((r >= _x) && (r <= tr))
		) && (
				((_y >= y) && (_y <= b)) || ((tb >= y) && (tb <= b)) ||
						((y >= _y) && (y <= tb)) || ((b >= _y) && (b <= tb))
		);
	}

	@Override
	public String toString()
	{
		return "(" + _template.getName() + " id=" + _objectId + " q=" + _q + ")";
	}
}
