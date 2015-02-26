package com.a2client.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * инвентарь
 * Created by arksu on 26.02.15.
 */
public class Inventory
{
    private static final Logger _log = LoggerFactory.getLogger(Inventory.class.getName());

    /**
     * объект родитель (внутри объекта может быть несколько вложенных инвентарей, вещи внутри вещей, типа сумок)
     */
    private int _parentObjectId;

    /**
     * ид объекта или вещи чей это инвентарь
     */
    private int _inventoryId;

    /**
     * содержимое
     */
    private Map<Integer, InventoryItem> _items = new HashMap<>();

    public Inventory(int parentObjectId, int inventoryId)
    {
        _parentObjectId = parentObjectId;
        _inventoryId = inventoryId;
    }

    public Map<Integer, InventoryItem> getItems()
    {
        return _items;
    }

    /**
     * добавить вещь в инвентарь или заменит если уже есть с таким же ид
     * @param item
     */
    public void addItem(InventoryItem item)
    {
        _items.put(item.getObjectId(), item);
    }

    public void clear() {
        _items.clear();
    }

    public int getParentObjectId()
    {
        return _parentObjectId;
    }

    public int getInventoryId()
    {
        return _inventoryId;
    }
}
