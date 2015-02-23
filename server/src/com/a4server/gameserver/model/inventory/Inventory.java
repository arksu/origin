package com.a4server.gameserver.model.inventory;

import com.a4server.gameserver.model.GameObject;
import javolution.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * инвентарь объекта
 * Created by arksu on 24.02.15.
 */
public class Inventory
{
    private static final Logger _log = LoggerFactory.getLogger(Inventory.class.getName());

    FastList<InventoryItem> _items = new FastList<>();

    public Inventory(GameObject parent)
    {

    }
}
