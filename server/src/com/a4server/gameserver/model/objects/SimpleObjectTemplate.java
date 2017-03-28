package com.a4server.gameserver.model.objects;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.objects.impl.ObjectClasses;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * реализация простого шаблона для большинства объектов
 * Created by arksu on 23.02.15.
 */
public class SimpleObjectTemplate implements ObjectTemplate
{
	private final String _name;
	private int _typeId;
	private int _width = 10;
	private int _height = 10;
	private Class<? extends GameObject> _class;
	private CollisionTemplate _collision = null;
	private InventoryTemplate _inventory = null;
	private ItemTemplate _item = null;

	private static final Logger _log = LoggerFactory.getLogger(SimpleObjectTemplate.class.getName());
	private static final Gson _gson = new Gson();

	public SimpleObjectTemplate(String name)
	{
		_name = name;
	}

	public void read(JsonReader in) throws IOException
	{
		while (in.hasNext())
		{
			JsonToken tkn = in.peek();
			switch (tkn)
			{
				case NAME:
					readParam(in);
					break;
				case END_OBJECT:
					return;
				default:
					_log.warn(getClass().getSimpleName() + ": wrong token " + tkn);
					return;
			}
		}

		// если это вещь и у нее есть инвентарь
		if (_item != null && _inventory != null)
		{
			// обновим шаблон вещи
			_item.setInventory(_inventory);
		}
	}

	protected void readParam(JsonReader in) throws IOException
	{
		String paramName = in.nextName();
		if ("typeid".equalsIgnoreCase(paramName))
		{
			_typeId = in.nextInt();
		}
		else if ("class".equalsIgnoreCase(paramName))
		{
			_class = ObjectClasses.getClass(in.nextString());
		}
		else if ("size".equalsIgnoreCase(paramName))
		{
			int sz = in.nextInt();
			_width = sz;
			_height = sz;
		}
		else if ("collision".equalsIgnoreCase(paramName))
		{
			_collision = _gson.fromJson(in, CollisionTemplate.class);
		}
		else if ("inventory".equalsIgnoreCase(paramName))
		{
			_inventory = _gson.fromJson(in, InventoryTemplate.class);
		}
		else if ("item".equalsIgnoreCase(paramName))
		{
			_item = ItemTemplate.load(in, _typeId, _name, this);
			ObjectsFactory.getInstance().addItemTemplate(_item.getTypeId(), _item);
		}
	}

	@Override
	public int getTypeId()
	{
		return _typeId;
	}

	@Override
	public int getWidth()
	{
		return _width;
	}

	@Override
	public int getHeight()
	{
		return _height;
	}

	@Override
	public String getName()
	{
		return _name;
	}

	@Override
	public Class<? extends GameObject> getClazz()
	{
		return _class;
	}

	@Override
	public CollisionTemplate getCollision()
	{
		return _collision;
	}

	@Override
	public InventoryTemplate getInventory()
	{
		return _inventory;
	}

	@Override
	public ItemTemplate getItem()
	{
		return _item;
	}
}
