package com.a2client.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 17.09.15.
 */
public class EquipSlot
{
	private static final Logger _log = LoggerFactory.getLogger(EquipSlot.class.getName());

	/**
	 * ид объекта вещи
	 */
	private final int _objectId;

	private final int _slotCode;

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

	public EquipSlot(int slotCode, int objectId, int typeId, String icon,
					 int q, int w, int h, int stage, int amount,
					 int ticks, int ticksTotal)
	{
		_slotCode = slotCode;
		_objectId = objectId;
		_typeId = typeId;
		_icon = icon;
		_q = q;
		_w = w;
		_h = h;
		_stage = stage;
		_amount = amount;
		_ticks = ticks;
		_ticksTotal = ticksTotal;
	}
}
