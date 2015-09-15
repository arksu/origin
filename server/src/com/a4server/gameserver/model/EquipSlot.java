package com.a4server.gameserver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 14.09.15.
 */
public class EquipSlot
{
	private static final Logger _log = LoggerFactory.getLogger(EquipSlot.class.getName());

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
