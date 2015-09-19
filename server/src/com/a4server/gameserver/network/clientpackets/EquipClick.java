package com.a4server.gameserver.network.clientpackets;

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

	}
}
