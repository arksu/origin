package com.a4server.gameserver.model;

import com.a4server.gameserver.model.inventory.AbstractItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * рука в которой игрок держит вещь (inventory item)
 * Created by arksu on 09.09.15.
 */
public class Hand
{
	private static final Logger _log = LoggerFactory.getLogger(Hand.class.getName());

	/**
	 * вещь которую держим в руке
	 */
	private final AbstractItem _item;

	private final Player _player;

	/**
	 * отступ в координатах инвентаря (чтобы положить обратно корректно)
	 */
	private final int _offsetX;
	private final int _offsetY;

	/**
	 * отступ в координатах мыши (внутри вещи куда тыкнули мышью)
	 */
	private final int _mx;
	private final int _my;

	public Hand(Player player, AbstractItem item, int offsetX, int offsetY, int mx, int my)
	{
		_player = player;
		_item = item;
		_offsetX = offsetX;
		_offsetY = offsetY;
		_mx = mx;
		_my = my;
	}

	public AbstractItem getItem()
	{
		return _item;
	}

	public int getOffsetX()
	{
		return _offsetX;
	}

	public int getOffsetY()
	{
		return _offsetY;
	}

	public int getMx()
	{
		return _mx;
	}

	public int getMy()
	{
		return _my;
	}
}
