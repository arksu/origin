package com.a4server.gameserver.network.serverpackets;

import com.a4server.gameserver.model.inventory.Inventory;
import com.a4server.gameserver.model.inventory.InventoryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * шлем апдейт инвентаря. если не открыт - надо создать окошко и показать
 * Created by arksu on 26.02.15.
 */
public class InventoryUpdate extends GameServerPacket
{
	Inventory _inventory;

	public InventoryUpdate(Inventory inventory)
	{
		if (inventory == null)
		{
			throw new RuntimeException("null inventory");
		}
		_inventory = inventory;
	}

	@Override
	protected void write()
	{
		writeC(0x18);
		writeD(_inventory.getObject().getObjectId());
		writeD(_inventory.getInvenroyId());
		writeH(_inventory.getWidth());
		writeH(_inventory.getHeight());
		Map<Integer, InventoryItem> items = _inventory.getItems();
		writeH(items.size());
		if (items.size() > 0)
		{
			for (InventoryItem item : items.values())
			{
				writeD(item.getObjectId());
				writeD(item.getTemplate().getItemId());
				writeS(item.getTemplate().getIconName());
				writeD(item.getQ());
				writeC(item.getX());
				writeC(item.getY());
				writeC(item.getWidth());
				writeC(item.getHeight());
				writeH(item.getAmount());
				writeC(item.getStage());
				writeH(item.getTicks());
				writeH(item.getTicksTotal());
			}
		}
	}
}
