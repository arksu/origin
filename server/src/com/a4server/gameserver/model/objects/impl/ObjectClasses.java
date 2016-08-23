package com.a4server.gameserver.model.objects.impl;

import com.a4server.gameserver.model.GameObject;

import java.util.HashMap;
import java.util.Map;

/**
 * тут храним классы реализации объектов
 * Created by arksu on 22.08.16.
 */
public class ObjectClasses
{
	private static Map<String, Class<? extends GameObject>> _classes = new HashMap<>();

	public static void init()
	{
		_classes.put("tree", Tree.class);
		_classes.put("stone", Stone.class);
	}

	public static Class<? extends GameObject> getClass(String name)
	{
		return _classes.get(name);
	}
}
