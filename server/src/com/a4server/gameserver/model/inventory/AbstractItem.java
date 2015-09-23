package com.a4server.gameserver.model.inventory;

import com.a4server.Database;
import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.objects.InventoryTemplate;
import com.a4server.gameserver.model.objects.ItemTemplate;
import com.a4server.gameserver.model.objects.ObjectsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * абстрактная игровая вещь, используется в инвентарях и эквипе
 * Created by arksu on 15.09.15.
 */
public class AbstractItem
{
	private static final Logger _log = LoggerFactory.getLogger(AbstractItem.class.getName());

	/**
	 * обновить позицию вещи в инвентаре
	 */
	public static final String UPDATE_ITEM_XY = "UPDATE items SET x=?, y=? WHERE id=?";

	/**
	 * уникальный ид объекта (вещи)
	 */
	protected final int _objectId;

	/**
	 * шаблон вещи
	 */
	protected final ItemTemplate _template;

	/**
	 * вещь также может содержать инвентарь (вложенный)
	 */
	protected Inventory _inventory;

	/**
	 * качество
	 */
	protected int _q;

	/**
	 * место расположения
	 */
	protected int _x;
	protected int _y;

	/**
	 * количество
	 */
	protected int _amount;

	protected int _stage;
	protected int _ticks;
	protected int _ticksTotal;

	/**
	 * создать вещь на основании другой (сделать копию, привести к другому типу)
	 */
	public AbstractItem(AbstractItem other)
	{
		_objectId = other.getObjectId();
		_template = other.getTemplate();
		_q = other.getQ();
		_x = other.getX();
		_y = other.getY();
		_amount = other.getAmount();
		_stage = other.getStage();
		_ticks = other.getTicks();
		_ticksTotal = other.getTicksTotal();
		_inventory = other.getInventory();
	}

	/**
	 * грузим из базы вещь
	 * @param object объект к которому относится вещь
	 * @param rset строка из таблицы items
	 * @throws SQLException
	 */
	public AbstractItem(GameObject object, ResultSet rset) throws SQLException
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
			_inventory = new Inventory(object, _objectId, template);
		}
	}

	/**
	 * сохранить вещь в бд (обновить ее состояние)
	 */
	public void save()
	{

	}

	public int getObjectId()
	{
		return _objectId;
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

	@SuppressWarnings("SuspiciousNameCombination")
	public void setXY(int x, int y)
	{
		if (_x != x || _y != y)
		{
			// query queue
			try (Connection con = Database.getInstance().getConnection();
				 PreparedStatement statement = con.prepareStatement(UPDATE_ITEM_XY))
			{
				statement.setInt(1, x);
				statement.setInt(2, y);
				statement.setInt(3, _objectId);
				statement.executeUpdate();
				con.close();
			}
			catch (Exception e)
			{
				_log.warn("failed update xy item pos " + toString());
			}
			_x = x;
			_y = y;
		}
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
