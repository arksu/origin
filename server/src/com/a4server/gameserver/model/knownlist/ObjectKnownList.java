package com.a4server.gameserver.model.knownlist;

import com.a4server.gameserver.model.GameObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * список игровых объектов о которых знает другой объект
 * то есть эти объекты проецируются на клиент, список всех видимых объектов с точки зрения клиента
 * Created by arksu on 08.03.17.
 */
public class ObjectKnownList
{
	private final GameObject _activeObject;

	private final Map<Integer, GameObject> _knownObjects = new ConcurrentHashMap<>();

	public ObjectKnownList(GameObject activeObject)
	{
		_activeObject = activeObject;
	}

	public GameObject getActiveObject()
	{
		return _activeObject;
	}

	public Map<Integer, GameObject> getKnownObjects()
	{
		return _knownObjects;
	}

	public boolean isKnownObject(GameObject object)
	{
		return object != null && getKnownObjects().containsKey(object.getObjectId());
	}

	/**
	 * добавить объект в список
	 * обязательно будет проверка на видимость (дистанция и другие условия)
	 */
	public boolean addKnownObject(GameObject object)
	{
		if (object == null)
		{
			return false;
		}

		if (isKnownObject(object))
		{
			return false;
		}

		// проверка дистанции до объекта

		return getKnownObjects().put(object.getObjectId(), object) == null;
	}

	public boolean removeKnownObject(GameObject object)
	{
		if (object == null)
		{
			return false;
		}

		return getKnownObjects().remove(object.getObjectId()) != null;
	}
}
