package com.a4server.gameserver.network.serverpackets;

import com.a4server.gameserver.model.Equip;
import com.a4server.gameserver.model.EquipItem;

/**
 * Created by arksu on 16.09.15.
 */
public class EquipUpdate extends GameServerPacket
{
	final Equip _equip;

	public EquipUpdate(Equip equip)
	{
		_equip = equip;
	}

	@Override
	protected void write()
	{
		writeC(0x1D);
		writeC(_equip.getItems().size());
		for (EquipItem slot : _equip.getItems().values())
		{
			writeC(slot.getCode());

			writeD(slot.getObjectId());
			writeD(slot.getTemplate().getItemId());
			writeS(slot.getTemplate().getIconName());
			writeD(slot.getQ());
			writeC(slot.getWidth());
			writeC(slot.getHeight());
			writeH(slot.getAmount());
			writeC(slot.getStage());
			writeH(slot.getTicks());
			writeH(slot.getTicksTotal());
		}
	}
}
