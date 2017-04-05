package com.a4server.gameserver.model.objects;

import com.a4server.gameserver.idfactory.IdFactory;
import com.a4server.gameserver.model.GameObject;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * фарбрика объектов, создание объекта по его тип ид
 * Created by arksu on 15.02.15.
 */
public class ObjectsFactory
{
	private static final Logger _log = LoggerFactory.getLogger(ObjectsFactory.class.getName());

	private static final String CONFIG_NAME = "/objects.json";

	private boolean _isLoaded = false;
	private Map<Integer, ObjectTemplate> _templates = new HashMap<>();
	private Map<Integer, ItemTemplate> _itemTemplates = new HashMap<>();

	public void loadInternalConfig()
	{
		if (_isLoaded)
		{
			throw new IllegalStateException("ObjectsFactory already loaded");
		}
		_isLoaded = true;
		InputStream ins = this.getClass().getResourceAsStream(CONFIG_NAME);
		try
		{
			JsonReader in = new JsonReader(new InputStreamReader(ins, "UTF-8"));
			in.beginObject();
			while (in.hasNext())
			{
				JsonToken tkn = in.peek();
				switch (tkn)
				{
					case NAME:
						String name = in.nextName();
						_log.debug("object: " + name);
						ObjectTemplate template = readObjectTemplate(name, in);
						_templates.put(template.getTypeId(), template);
						break;
					default:
						_log.warn("loadInternalConfig: wrong token " + tkn.name());
						break;
				}
			}
			in.endObject();
			in.close();
		}
		catch (UnsupportedEncodingException e)
		{
			_log.error("loadInternalConfig: unsupported encoding", e);
		}
		catch (IOException e)
		{
			_log.error("loadInternalConfig: io error", e);
		}
		catch (Exception e)
		{
			_log.error("loadInternalConfig unexpected error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	protected ObjectTemplate readObjectTemplate(String name, JsonReader in) throws IOException
	{
		SimpleObjectTemplate template = new SimpleObjectTemplate(name);
		in.beginObject();
		template.read(in);
		in.endObject();
		return template;
	}

	public void addItemTemplate(int itemId, ItemTemplate template)
	{
		_itemTemplates.put(itemId, template);
	}

	public ItemTemplate getItemTemplate(int itemId)
	{
		ItemTemplate template = _itemTemplates.get(itemId);
		if (template == null) _log.warn("item template not found: " + itemId);
		return template;
	}

	public ObjectTemplate getTemplate(int typeId)
	{
		return _templates.get(typeId);
	}

	public ObjectTemplate getTemplate(String name)
	{
		for (ObjectTemplate template : _templates.values())
		{
			if (template.getName().equals(name))
			{
				return template;
			}
		}
		return null;
	}

	/**
	 * создать новый объект по его типу
	 * @param typeId тип нового объекта
	 * @return инстанс объекта, он никуда не добавлен (нет координат). но у него есть уникальный ид.
	 */
	public GameObject createObject(int typeId)
	{
		ObjectTemplate template = getTemplate(typeId);
		Class<? extends GameObject> clazz = template.getClazz();
		int id = IdFactory.getInstance().getNextId();
		GameObject object;
		if (clazz != null)
		{
			try
			{
				object = clazz.getDeclaredConstructor(int.class, ObjectTemplate.class).newInstance(id, template);
			}
			catch (Exception e)
			{
				throw new RuntimeException("failed create game object type=" + typeId + " " + e.getMessage(), e);
			}
		}
		else
		{
			object = new GameObject(id, template);
		}
		return object;
	}

	public static ObjectsFactory getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final ObjectsFactory _instance = new ObjectsFactory();
	}
}
