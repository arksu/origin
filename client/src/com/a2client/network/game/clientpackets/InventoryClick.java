package com.a2client.network.game.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 26.02.15.
 */
public class InventoryClick extends GameClientPacket
{
    private static final Logger _log = LoggerFactory.getLogger(InventoryClick.class.getName());

    int _inventoryId;
    int _objectId;

    public InventoryClick(int inventoryId, int objectId)
    {
        _inventoryId = inventoryId;
        _objectId = objectId;
    }

    @Override
    protected void write()
    {
        writeC(0x19);
        writeD(_inventoryId);
        writeD(_objectId);
    }
}
