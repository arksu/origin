package com.a4server.gameserver.model.inventory;

import com.a4server.Database;
import com.a4server.gameserver.model.GameObject;
import javolution.util.FastList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * инвентарь объекта
 * Created by arksu on 24.02.15.
 */
public class Inventory
{
    private static final Logger _log = LoggerFactory.getLogger(Inventory.class.getName());

    public static final String LOAD_INVENTORY = "SELECT id, itemId, x, y, q, amount, stage, ticks, ticksTotal FROM items WHERE objectId=?";

    /**
     * объект родитель
     */
    private final GameObject _parent;

    /**
     * ид инвентаря, объект или вещь к которой он относится
     */
    private final int _invenroyId;

    /**
     * инвентарь родитель
     */
    private final Inventory _inventory;

    /**
     * список вещей которые находятся внутри
     */
    FastList<InventoryItem> _items = new FastList<>();

    public Inventory(GameObject parent)
    {
        _parent = parent;
        _inventory = null;
        _invenroyId = parent.getObjectId();
        load(_parent.getObjectId());
    }

    public Inventory(Inventory parent, int objectId)
    {
        _inventory = parent;
        _invenroyId = objectId;
        _parent = null;
        load(objectId);
    }

    /**
     * загрузить инвентарь из базы
     * @param objectId ид объекта или вещи в котором хранится инвентарь
     */
    private void load(int objectId)
    {
        _log.debug("load inventory: " + objectId);
        try
        {
            try (Connection con = Database.getInstance().getConnection();
                 PreparedStatement ps = con.prepareStatement(LOAD_INVENTORY))
            {
                ps.setInt(1, objectId);
                try (ResultSet rset = ps.executeQuery())
                {
                    while (rset.next())
                    {
                        _items.add(new InventoryItem(this, rset));
                    }
                }
            }
        }
        catch (SQLException e)
        {
            _log.warn("Cant load inventory " + toString());
            throw new RuntimeException("Cant load inventory " + toString());
        }
    }

    public GameObject getParent()
    {
        return _parent;
    }

    public FastList<InventoryItem> getItems()
    {
        return _items;
    }

    public int getInvenroyId()
    {
        return _invenroyId;
    }
}
