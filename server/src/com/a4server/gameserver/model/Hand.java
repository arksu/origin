package com.a4server.gameserver.model;

import com.a4server.gameserver.model.inventory.InventoryItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * рука в которой игрок держит вещь (inventory item)
 * Created by arksu on 09.09.15.
 */
public class Hand
{
	private static final Logger _log = LoggerFactory.getLogger(Hand.class.getName());

	private final InventoryItem _item;

	private final Player _player;

	public Hand(Player player, InventoryItem item)
	{
		_player = player;
		_item = item;
	}

	public InventoryItem getItem()
	{
		return _item;
	}
}
