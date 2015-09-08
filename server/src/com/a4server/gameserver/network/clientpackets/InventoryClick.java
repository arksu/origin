package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.inventory.Inventory;
import com.a4server.gameserver.model.inventory.InventoryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * клик по объекту в инвентаре
 * Created by arksu on 26.02.15.
 */
public class InventoryClick extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(InventoryClick.class.getName());

	private int _inventoryId;
	private int _objectId;
	private int _btn;
	private int _mod;
	private int _offsetX;
	private int _offsetY;

	@Override
	public void readImpl()
	{
		_inventoryId = readD();
		_objectId = readD();
		_btn = readC();
		_mod = readC();
		_offsetX = readC();
		_offsetY = readC();
	}

	@Override
	public void run()
	{
		Player player = client.getActiveChar();
		if (player != null && _btn == 0)
		{
			_log.debug("InventoryClick: obj=" + _objectId + " inv=" + _inventoryId + " offset=" + _offsetX + ", " + _offsetY);

			// держим в руке что-то?
			if (player.getHand() == null)
			{
				// в руке ничего нет. возьмем из инвентаря
				// надо найти объект инвентаря по его ид
				Inventory from;
				InventoryItem item;
				if (player.getInventory() != null)
				{
					item = player.getInventory().takeItem(_objectId);
				}
			}
			else
			{
				// положим в инвентарь
			}
		}
	}
}
