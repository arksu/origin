package com.a4server.gameserver.model.objects;

import com.a4server.gameserver.model.inventory.AbstractItem;
import com.google.gson.annotations.SerializedName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * шаблон инвентаря
 * Created by arksu on 24.02.15.
 */
public class InventoryTemplate
{
	private static final Logger _log = LoggerFactory.getLogger(InventoryTemplate.class.getName());

	@SerializedName("width")
	private int _width = 2;

	@SerializedName("height")
	private int _height = 2;

	@SerializedName("acceptAll")
	private boolean _acceptAll = true;

	@SerializedName("exclude")
	private List<String> _exclude = new ArrayList<>();

	public InventoryTemplate(int width, int height)
	{
		_width = width;
		_height = height;
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
	 * можно ли положить указанную вещь в этот инвентарь?
	 */
	public boolean isAccept(AbstractItem item)
	{
		if (_acceptAll)
		{
			return !_exclude.contains(item.getTemplate().getName());
		}
		else
		{
			return _exclude.contains(item.getTemplate().getName());
		}
	}

	@Override
	public String toString()
	{
		return "(inventory " + _width + "x" + _height + ")";
	}
}
