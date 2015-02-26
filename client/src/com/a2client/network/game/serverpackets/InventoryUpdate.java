package com.a2client.network.game.serverpackets;

import com.a2client.model.InventoryItem;
import com.a2client.network.game.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 26.02.15.
 */
public class InventoryUpdate extends GameServerPacket
{
    static
    {
        GamePacketHandler.AddPacketType(0x18, InventoryUpdate.class);
    }

    private static final Logger _log = LoggerFactory.getLogger(InventoryUpdate.class.getName());

    int _parentObjectId;
    List<InventoryItem> _items = new ArrayList<>();

    @Override
    public void readImpl()
    {
        _parentObjectId = readD();
        int size = readH();
        while (size > 0)
        {
            size--;

            int objectId = readD();
            int typeId = readD();
            int q = readD();
            int x = readC();
            int y = readC();
            int amount = readH();
            int stage = readC();
            int ticks = readH();
            int ticksTotal = readH();

            _items.add(new InventoryItem(objectId, typeId, q, x, y, stage, amount, ticks, ticksTotal));
        }
    }

    @Override
    public void run()
    {

    }
}
