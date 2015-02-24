package com.a4server.gameserver.model.inventory;

import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.objects.ItemTemplate;
import com.a4server.gameserver.model.objects.ObjectsFactory;
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

    /**
     * инвентарь в котором хранится данная вещь
     */
    private Inventory _parent;

    /**
     * уникальный ид объекта (вещи)
     */
    private int _objectId;

    /**
     * шаблон вещи
     */
    private ItemTemplate _template;

    /**
     * качество
     */
    private int _q;

    /**
     * место расположения
     */
    private int _x;
    private int _y;


    private int _amount;
    private int _stage;
    private int _ticks;
    private int _ticksTotal;

    /**
     * вещь также может содержать инвентарь (вложенный)
     */
    private Inventory _inventory;

    public InventoryItem(Inventory parent, ResultSet rset)
    {
        _parent = parent;
        try
        {
            _objectId = rset.getInt("id");
            _template = ObjectsFactory.getInstance().getItemTemplate(rset.getInt("itemId"));
            _q = rset.getInt("q");
            _x = rset.getInt("x");
            _y = rset.getInt("y");
            _amount = rset.getInt("amount");
            _stage = rset.getInt("stage");
            _ticks = rset.getInt("ticks");
            _ticksTotal = rset.getInt("ticksTotal");

            _inventory = new Inventory(_parent, _objectId);
        }
        catch (SQLException e)
        {
            _log.error("failed load item " + e.getMessage());
        }
    }

    public ItemTemplate getTemplate()
    {
        return _template;
    }

    @Override
    public String toString()
    {
        return "("+_objectId+" q="+_q+")";
    }
}
