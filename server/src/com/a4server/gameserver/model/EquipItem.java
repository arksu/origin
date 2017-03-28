package com.a4server.gameserver.model;

import com.a4server.gameserver.model.inventory.AbstractItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * слот эквипа
 * Created by arksu on 14.09.15.
 */
public class EquipItem extends AbstractItem
{
	private static final Logger _log = LoggerFactory.getLogger(EquipItem.class.getName());

	/**
	 * слот эквипа
	 */
	public enum Slot
	{
		LHAND(0), RHAND(1),
		HEAD(2), BODY(3),
		PANTS(6),
		LFOOT(4), RFOOT(5);

		private final int _code;

		Slot(int code)
		{
			_code = code;
		}

		public int getCode()
		{
			return _code;
		}
	}

	/**
	 * слот в котором находится вещь
	 */
	private Slot _slot;

	/**
	 * грузим из базы вещь
	 * @param object объект к которому относится вещь
	 * @param rset строка из таблицы items
	 * @throws SQLException
	 */
	public EquipItem(GameObject object, ResultSet rset) throws SQLException
	{
		super(object, rset);
		// код слота храним в y
		_slot = getSlotType(_y);
	}

	/**
	 * создать копию на базе другой вещи (привести к типу)
	 */
	public EquipItem(AbstractItem other)
	{
		super(other);
	}

	/**
	 * получить слот по его коду
	 */
	public static Slot getSlotType(int code)
	{
		for (Slot slot : Slot.values())
		{
			if (slot.getCode() == code)
			{
				return slot;
			}
		}
		_log.warn("unknown slot for code: " + code);
		return null;
	}

	public Slot getSlot()
	{
		return _slot;
	}

	public void setSlot(Slot slot)
	{
		_slot = slot;
		setXY(200, slot.getCode());
	}

	public int getCode()
	{
		return _slot.getCode();
	}
}
