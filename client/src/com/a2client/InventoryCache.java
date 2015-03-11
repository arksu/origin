package com.a2client;

import com.a2client.gui.GUI;
import com.a2client.guigame.GUI_InventoryWindow;
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

    private Map<Integer, Inventory> _inventories = new HashMap<>();

    /**
     * открытые инвентари
     */
    private Map<Integer, GUI_InventoryWindow> _openInventories = new HashMap<>();

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

    /**
     * открыть инвентарь (отобразить окно с этим инвентарем)
     * @param inventoryId ид инвентаря
     */
    public void openInventory(int inventoryId)
    {
        // только если такой инвентарь еще не открыт
        if (!_openInventories.containsKey(inventoryId))
        {
            GUI_InventoryWindow wnd = new GUI_InventoryWindow(GUI.rootNormal(), inventoryId);
            wnd.SetPos(100, 100);
            _openInventories.put(inventoryId, wnd);
        }
    }

    /**
     * закрыть указанный инвентарь
     * @param inventoryId ид инвентаря
     */
    public void closeInventory(int inventoryId)
    {
        GUI_InventoryWindow wnd = _openInventories.get(inventoryId);
        if (wnd != null)
        {
            wnd.Unlink();
            _openInventories.remove(inventoryId);
        }
    }

    public static InventoryCache getInstance()
    {
        return _instance;
    }
}
