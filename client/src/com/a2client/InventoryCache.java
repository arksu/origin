package com.a2client;

import com.a2client.model.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * кэш для инвентарей которые посылает нам сервер
 * Created by arksu on 26.02.15.
 */
public class InventoryCache
{
	private static final Logger _log = LoggerFactory.getLogger(InventoryCache.class.getName());

	private static final InventoryCache _instance = new InventoryCache();

	private final Map<Integer, Inventory> _inventories = new HashMap<>();

	/**
	 * получить инвентарь
	 * @param objectId ид инвентаря
	 * @return инвентарь если он найден
	 */
	public Inventory get(int objectId)
	{
		return _inventories.get(objectId);
	}

	/**
	 * добавить инвентарь в кэш
	 * @param inventory инвентарь
	 */
	public void add(Inventory inventory)
	{
		_inventories.put(inventory.getInventoryId(), inventory);
	}

	public void clear()
	{
		_inventories.clear();
	}

	public static InventoryCache getInstance()
	{
		return _instance;
	}
}
