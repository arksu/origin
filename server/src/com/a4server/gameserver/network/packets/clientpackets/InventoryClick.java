package com.a4server.gameserver.network.packets.clientpackets;

import com.a4server.gameserver.model.GameLock;
import com.a4server.gameserver.model.GameObject;
import com.a4server.gameserver.model.Hand;
import com.a4server.gameserver.model.Player;
import com.a4server.gameserver.model.inventory.AbstractItem;
import com.a4server.gameserver.model.inventory.Inventory;
import com.a4server.gameserver.model.inventory.InventoryItem;
import com.a4server.gameserver.network.packets.serverpackets.InventoryUpdate;
import com.a4server.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.a4server.gameserver.model.GameObject.WAIT_LOCK;

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

	/**
	 * отступ в пикселах внутри вещи где произошел клик
	 */
	private int _offsetX;
	private int _offsetY;

	/**
	 * слот в который тыкнули
	 */
	private int _x;
	private int _y;

	@Override
	public void readImpl()
	{
		_inventoryId = readD();
		_objectId = readD();
		_btn = readC();
		_mod = readC();
		_offsetX = readC();
		_offsetY = readC();
		_x = readC();
		_y = readC();
	}

	@Override
	public void run()
	{
		_log.debug("obj=" + _objectId + " inv=" + _inventoryId + " (" + _x + ", " + _y + ")" + " offset=" + _offsetX + ", " + _offsetY + " mod=" + _mod);
		Player player = client.getPlayer();
		if (player != null && _btn == 0)
		{
			try (GameLock ignored = player.lock())
			{
				// держим в руке что-то?
				if (player.getHand() == null)
				{
					// в руке ничего нет. возьмем из инвентаря
					InventoryItem item = null;
					if (player.getInventory() != null)
					{
						item = player.getInventory().findItem(_objectId);
					}
					// не нашли эту вещь у себя в инвентаре
					// попробуем найти в объекте с которым взаимодействуем
					if (item == null && player.isInteractive())
					{
						for (GameObject object : player.getInteractWith())
						{
							item = object.getInventory() != null ? object.getInventory().findItem(_objectId) : null;
							if (item != null)
							{
								break;
							}
						}
					}

					// пробуем взять вещь из инвентаря
					if (item != null)
					{
						GameObject object = item.getParentInventory().getObject();
						try (GameLock ignored2 = object.lock())
						{
							InventoryItem taked = item.getParentInventory().takeItem(item);

							// взяли вещь из инвентаря
							if (taked != null)
							{
								object.sendInteractPacket(new InventoryUpdate(item.getParentInventory()));
								// какая кнопка была зажата
								switch (_mod)
								{
									case Utils.MOD_ALT:
										// сразу перекинем вещь в инвентарь
										if (!putItem(player, taked, -1, -1))
										{
											setHand(player, taked);
										}
										break;

									default:
										// ничего не нажато. пихаем в руку
										setHand(player, taked);
										break;
								}
							}
						}
					}
					else
					{
						// возможно какой-то баг или ошибка. привлечем внимание
						_log.error("InventoryClick: item=null");
					}
				}
				else
				{
					// положим в инвентарь то что держим в руке
					Hand hand = player.getHand();
					if (putItem(player, hand.getItem(), _x - hand.getOffsetX(), _y - hand.getOffsetY()))
					{
						player.setHand(null);
					}
				}
			}
		}
	}

	/**
	 * положить вещь в инвентарь
	 * @param item вещь которую кладем
	 */
	public boolean putItem(Player player, AbstractItem item, int x, int y)
	{
		Inventory to = null;
		// ищем нужный инвентарь у себя
		if (player.getInventory() != null)
		{
			to = player.getInventory().findInventory(_inventoryId);
		}
		// а потом в объектах с которыми взаимодействую
		if (to == null && player.isInteractive())
		{
			for (GameObject object : player.getInteractWith())
			{
				to = object.getInventory() != null ? object.getInventory().findInventory(_inventoryId) : null;
				if (to != null)
				{
					break;
				}
			}
		}

		// положим в инвентарь
		if (to != null)
		{
			if (to.getObject().tryLock(WAIT_LOCK))
			{
				try
				{
					InventoryItem putItem = to.putItem(item, x, y);
					if (putItem != null)
					{
						to.getObject().sendInteractPacket(new InventoryUpdate(to));
						return true;
					}
				}
				finally
				{
					to.getObject().unlock();
				}
			}
		}
		return false;
	}

	private void setHand(Player player, InventoryItem taked)
	{
		player.setHand(new Hand(player, taked,
		                        _x - taked.getX(),
		                        _y - taked.getY(),
		                        _offsetX, _offsetY
		));
	}
}
