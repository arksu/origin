package com.a2client.network.game.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 20.09.15.
 */
public class EquipClick extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(EquipClick.class.getName());

	final int _objectId;
	final int _btn;
	final int _mod;
	final int _offsetX;
	final int _offsetY;
	final int _slotCode;

	public EquipClick(int objectId, int btn, int mod, int offsetX, int offsetY, int slotCode)
	{
		_objectId = objectId;
		_btn = btn;
		_mod = mod;
		_offsetX = offsetX;
		_offsetY = offsetY;
		_slotCode = slotCode;
	}

	@Override
	protected void write()
	{
		writeC(0x1E);

		writeD(_objectId);
		writeC(_btn);
		writeC(_mod);
		writeC(_offsetX);
		writeC(_offsetY);
		writeC(_slotCode);
	}
}
