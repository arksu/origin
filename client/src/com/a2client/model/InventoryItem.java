package com.a2client.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * вещь в инвентаре
 * Created by arksu on 26.02.15.
 */
public class InventoryItem
{
    private static final Logger _log = LoggerFactory.getLogger(InventoryItem.class.getName());

    private final int _objectId;
    private final int _typeId;
    private final int _q;
    private final int _x;
    private final int _y;
    private final int _stage;
    private final int _amount;
    private final int _ticks;
    private final int _ticksTotal;

    public InventoryItem(int objectId, int typeId, int q, int x, int y, int stage, int amount,
                         int ticks, int ticksTotal) {
        _objectId = objectId;
        _typeId = typeId;
        _q = q;
        _x = x;
        _y= y;
        _stage = stage;
        _amount = amount;
        _ticks = ticks;
        _ticksTotal = ticksTotal;
    }

    public int getObjectId()
    {
        return _objectId;
    }

    public int getTypeId()
    {
        return _typeId;
    }

    public int getQ()
    {
        return _q;
    }

    public int getX()
    {
        return _x;
    }

    public int getY()
    {
        return _y;
    }

    public int getStage()
    {
        return _stage;
    }

    public int getAmount()
    {
        return _amount;
    }

    public int getTicks()
    {
        return _ticks;
    }

    public int getTicksTotal()
    {
        return _ticksTotal;
    }
}
