package com.a2client.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * вещь в инвентаре
 * Created by arksu on 26.02.15.
 */
public class InventoryItem
{
	private static final Logger _log = LoggerFactory.getLogger(InventoryItem.class.getName());

	/**
	 * размеры слота в пикселах
	 */
	public static final int WIDTH = 32;
	public static final int HEIGHT = 32;

	/**
	 * отступ между слотами
	 */
	public static final int MARGIN = 1;

	/**
	 * инвентарь в котором лежит
	 */
	private final int _inventoryId;

	/**
	 * ид объекта вещи
	 */
	private final int _objectId;

	/**
	 * тип
	 */
	private final int _typeId;

	/**
	 * иконку которую используем для отображения вещи
	 */
	private final String _icon;

	/**
	 * качество
	 */
	private final int _q;

	/**
	 * координаты, размер
	 */
	private final int _x;
	private final int _y;
	private final int _w;
	private final int _h;

	/**
	 * стадия
	 */
	private final int _stage;

	/**
	 * количество
	 */
	private final int _amount;

	private final int _ticks;
	private final int _ticksTotal;

	public InventoryItem(int inventoryId, int objectId, int typeId, String icon,
						 int q, int x, int y, int w, int h, int stage, int amount,
						 int ticks, int ticksTotal)
	{
		_inventoryId = inventoryId;
		_objectId = objectId;
		_typeId = typeId;
		_icon = icon;
		_q = q;
		_x = x;
		_y = y;
		_w = w;
		_h = h;
		_stage = stage;
		_amount = amount;
		_ticks = ticks;
		_ticksTotal = ticksTotal;
	}

	public int getInventoryId()
	{
		return _inventoryId;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public int getTypeId()
	{
		return _typeId;
	}

	public String getIcon()
	{
		return _icon;
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

	public int getWidth()
	{
		return _w;
	}

	public int getHeight()
	{
		return _h;
	}

	public int getStage()
	{
		return _stage;
	}

	public int getAmount()
	{
		return _amount;
	}

	public int getTicks()
	{
		return _ticks;
	}

	public int getTicksTotal()
	{
		return _ticksTotal;
	}
}
