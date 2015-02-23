package com.a4server.gameserver.model.inventory;

import com.a4server.gameserver.model.GameObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * вещь в инвентаре
 * Created by arksu on 23.02.15.
 */
public class InventoryItem
{
    private static final Logger _log = LoggerFactory.getLogger(InventoryItem.class.getName());

    private GameObject _parent;
    private int _objectId;
    private int _itemId;
    private int _q;
    private int _x;
    private int _y;
    private int _amount;
    private int _stage;
    private int _ticks;
    private int _ticksTotal;

    public InventoryItem(GameObject parent, ResultSet rset)
    {
        _parent = parent;
        try
        {
            _objectId = rset.getInt("id");
            _itemId = rset.getInt("itemId");
            _q = rset.getInt("q");
            _x = rset.getInt("x");
            _y = rset.getInt("y");
            _amount = rset.getInt("amount");
            _stage = rset.getInt("stage");
            _ticks = rset.getInt("ticks");
            _ticksTotal = rset.getInt("ticksTotal");
        }
        catch (SQLException e)
        {
            _log.error("failed load item " + e.getMessage());
        }
    }
}
