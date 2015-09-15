package com.a4server.gameserver.model;

import com.a4server.gameserver.model.inventory.AbstractItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by arksu on 14.09.15.
 */
public class EquipSlot extends AbstractItem
{
	private static final Logger _log = LoggerFactory.getLogger(EquipSlot.class.getName());

	/**
	 * грузим из базы вещь
	 * @param object объект к которому относится вещь
	 * @param rset строка из таблицы items
	 * @throws SQLException
	 */
	public EquipSlot(GameObject object, ResultSet rset) throws SQLException
	{
		super(object, rset);
	}

	public enum Slot
	{
		LHAND(0), RHAND(1), HEAD(2);

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

}
