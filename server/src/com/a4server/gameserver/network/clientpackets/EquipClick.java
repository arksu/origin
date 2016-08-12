package com.a4server.gameserver.network.clientpackets;

import com.a4server.gameserver.model.EquipSlot;
import com.a4server.gameserver.model.GameLock;
import com.a4server.gameserver.model.Hand;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.network.serverpackets.EquipUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * клик в инвентаре по слоту
 * Created by arksu on 20.09.15.
 */
public class EquipClick extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(EquipClick.class.getName());

	int _objectId;
	int _btn;
	int _mod;

	/**
	 * отступ в пикселах внутри вещи где произошел клик
	 */
	int _offsetX;
	int _offsetY;

	int _slotCode;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_btn = readC();
		_mod = readC();
		_offsetX = readC();
		_offsetY = readC();
		_slotCode = readC();
	}

	@Override
	public void run()
	{
		EquipSlot.Slot slot = EquipSlot.getSlotType(_slotCode);
		_log.debug("obj=" + _objectId + " slot=" + slot + " offset=" + _offsetX + ", " + _offsetY + " mod=" + _mod);
		Player player = client.getPlayer();
		if (player != null && _btn == 0)
		{
			try (GameLock ignored = player.tryLock())
			{
				// держим в руке что-то?
				if (player.getHand() == null)
				{
					EquipSlot item = player.getEquip().getItems().remove(slot);
					if (item != null)
					{
						player.setHand(new Hand(player, item, 0, 0, _offsetX, _offsetY));
						player.getClient().sendPacket(new EquipUpdate(player.getEquip()));
					}
				}
				else
				{
					// положим вещь в слот
					if (player.getEquip().putItem(player.getHand().getItem(), slot))
					{
						player.setHand(null);
						player.getClient().sendPacket(new EquipUpdate(player.getEquip()));
					}
				}
			}
			catch (Exception e)
			{
				_log.error("EquipClick error: " + e.getMessage(), e);
			}
		}
	}
}
