package com.a4server.gameserver.network.serverpackets;

import com.a4server.gameserver.model.Equip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 16.09.15.
 */
public class EquipUpdate extends GameServerPacket
{
	private static final Logger _log = LoggerFactory.getLogger(EquipUpdate.class.getName());

	final Equip _equip;

	public EquipUpdate(Equip equip)
	{
		_equip = equip;
	}

	@Override
	protected void write()
	{
		writeC(0x1D);

	}
}
