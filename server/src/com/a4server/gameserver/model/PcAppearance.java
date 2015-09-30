package com.a4server.gameserver.model;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by arksu on 05.01.2015.
 */
public class PcAppearance
{
	/**
	 * представление игрока в мире. то как он выглядит
	 */

	private boolean _sex = false; // true = female; male = false;

	private byte _hairColor = 0;
	private byte _hairStyle = 0;
	private byte _face = 0;
	private int _objectId;

	public PcAppearance(ResultSet set, int objectId)
	{
		_objectId = objectId;
		try
		{
			_face = set.getByte("face");
			_hairColor = set.getByte("hairColor");
			_hairStyle = set.getByte("hairStyle");
			_sex = set.getInt("sex") != 0;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public byte getHairColor()
	{
		return _hairColor;
	}

	public byte getHairStyle()
	{
		return _hairStyle;
	}

	public byte getFace()
	{
		return _face;
	}

	public boolean isFemale()
	{
		return _sex;
	}
}
