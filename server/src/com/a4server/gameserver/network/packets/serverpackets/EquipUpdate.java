package com.a4server.gameserver.network.packets.serverpackets;

import com.a4server.gameserver.model.Equip;
import com.a4server.gameserver.model.EquipItem;

/**
 * Created by arksu on 16.09.15.
 */
public class EquipUpdate extends GameServerPacket
{
	private final Equip _equip;
	private final int _objectId;

	public EquipUpdate(int objectId, Equip equip)
	{
		_objectId = objectId;
		_equip = equip;
	}

	@Override
	protected void write()
	{
		writeC(0x1D);
		writeD(_objectId);
		writeC(_equip.getItems().size());
		for (EquipItem slot : _equip.getItems().values())
		{
			writeC(slot.getCode());

			writeD(slot.getObjectId());
			writeD(slot.getTemplate().getTypeId());
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
