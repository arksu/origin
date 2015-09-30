package com.a2client.network.game.serverpackets;

import com.a2client.Player;
import com.a2client.model.InventoryItem;
import com.a2client.network.game.GamePacketHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 17.09.15.
 */
public class EquipUpdate extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x1D, EquipUpdate.class);
	}

	List<InventoryItem> _items = new ArrayList<>();

	@Override
	public void readImpl()
	{
		int count = readC();
		while (count > 0)
		{
			count--;

			int slotCode = readC();
			int objectId = readD();
			int typeId = readD();
			String icon = readS();
			int q = readD();
			int w = readC();
			int h = readC();
			int amount = readH();
			int stage = readC();
			int ticks = readH();
			int ticksTotal = readH();
			_items.add(new InventoryItem(Player.getInstance().getObjectId(),
										 objectId, typeId, icon, q, 200, slotCode, w, h, stage, amount, ticks, ticksTotal));
		}
	}

	@Override
	public void run()
	{
		List<InventoryItem> equipItems = Player.getInstance().getEquip().getItems();
		equipItems.clear();
		equipItems.addAll(_items);
		// если есть открытый инвентарь - обновить содержимое
		Player.getInstance().getEquip().onChange();
	}
}
