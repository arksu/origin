package com.a2client.network.game.serverpackets;

import com.a2client.model.EquipSlot;
import com.a2client.network.game.GamePacketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger _log = LoggerFactory.getLogger(EquipUpdate.class.getName());

	List<EquipSlot> _items = new ArrayList<>();

	@Override
	public void readImpl()
	{
		int count = readC();
		while (count > 0) {
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
			_items.add(new EquipSlot(slotCode, objectId, typeId, icon, q, w, h, stage, amount, ticks, ticksTotal));
		}
	}

	@Override
	public void run()
	{

	}
}
