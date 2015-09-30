package com.a4server.gameserver.model.collision;

import com.a4server.gameserver.model.GameObject;
import com.a4server.util.Rect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * виртуальный объект. нужен для возможности поставить объект виртуально и получить с ним коллизию.
 * Created by arksu on 07.01.2015.
 */
public class VirtualObject
{
	private static final Logger _log = LoggerFactory.getLogger(VirtualObject.class.getName());

	private int _x, _y;
	private GameObject _type;

	public VirtualObject(int x, int y, GameObject type)
	{
		this._x = x;
		this._y = y;
		this._type = type;
	}

	public int getX()
	{
		return _x;
	}

	public int getY()
	{
		return _y;
	}

	public Rect getBoundRect()
	{
		return _type.getBoundRect();
	}

	public GameObject getObject()
	{
		return _type;
	}
}
