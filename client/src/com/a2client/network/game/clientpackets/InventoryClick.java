package com.a2client.network.game.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 26.02.15.
 */
public class InventoryClick extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(InventoryClick.class.getName());

	private final int _inventoryId;
	private final int _objectId;
	private final int _btn;
	private final int _mod;
	/**
	 * отступ в пикселах внутри вещи где произошел клик
	 */
	private final int _offsetX;
	private final int _offsetY;

	private final int _x;
	private final int _y;

	public InventoryClick(int inventoryId, int objectId, int btn, int mod, int offsetX, int offsetY, int x, int y)
	{
		_inventoryId = inventoryId;
		_objectId = objectId;
		_btn = btn;
		_mod = mod;
		_offsetX = offsetX;
		_offsetY = offsetY;
		_x = x;
		_y = y;
	}

	@Override
	protected void write()
	{
		writeC(0x19);
		writeD(_inventoryId);
		writeD(_objectId);
		writeC(_btn);
		writeC(_mod);
		writeC(_offsetX);
		writeC(_offsetY);
		writeC(_x);
		writeC(_y);
	}
}
