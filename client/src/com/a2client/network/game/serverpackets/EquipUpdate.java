package com.a2client.network.game.serverpackets;

import com.a2client.ObjectCache;
import com.a2client.PlayerData;
import com.a2client.model.Character;
import com.a2client.model.Equip;
import com.a2client.model.GameObject;
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

	private List<InventoryItem> _items = new ArrayList<>();
	private int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
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
			_items.add(new InventoryItem(PlayerData.getInstance().getObjectId(),
			                             objectId, typeId, icon, q, 200, slotCode, w, h, stage, amount, ticks, ticksTotal));
		}
	}

	@Override
	public void run()
	{
		// это мой эквип?
		if (_objectId == PlayerData.getInstance().getObjectId())
		{
			List<InventoryItem> equipItems = PlayerData.getInstance().getEquipWindow().getItems();
			equipItems.clear();
			equipItems.addAll(_items);
			// если есть открытый инвентарь - обновить содержимое
			PlayerData.getInstance().getEquipWindow().onChange();
		}

		GameObject object = ObjectCache.getInstance().getObject(_objectId);
		if (object != null && object instanceof Character)
		{
			List<Equip.Slot> binded = new ArrayList<>();
			Character character = (Character) object;
			for (InventoryItem item : _items)
			{
				binded.add(character.getEquip().bind(item));
			}

			List<Equip.Slot> toRemove = new ArrayList<>();
			for (Equip.Slot slot : character.getEquip().getEquipped().keySet())
			{
				if (!binded.contains(slot))
				{
					toRemove.add(slot);
				}
			}
			for (Equip.Slot slot : toRemove)
			{
				character.getEquip().unbind(slot);
			}
		}
	}
}
