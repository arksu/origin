package com.a4server.gameserver.model.knownlist;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Player;

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
	private final Map<Integer, Player> _knownPlayers = new ConcurrentHashMap<>();

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

	public Map<Integer, Player> getKnownPlayers()
	{
		return _knownPlayers;
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
//		if (getActiveObject().)

		boolean result = getKnownObjects().put(object.getObjectId(), object) == null;
		if (result && object.isPlayer())
		{
			getKnownPlayers().put(object.getObjectId(), object.getActingPlayer());
		}
		return result;
	}

	public boolean removeKnownObject(GameObject object)
	{
		if (object == null)
		{
			return false;
		}

		boolean result = getKnownObjects().remove(object.getObjectId()) != null;
		if (result && object.isPlayer())
		{
			getKnownPlayers().remove(object.getObjectId());
		}
		return result;
	}
}
