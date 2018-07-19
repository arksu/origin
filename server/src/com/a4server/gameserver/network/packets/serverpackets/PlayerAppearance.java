package com.a4server.gameserver.network.packets.serverpackets;

import com.a4server.gameserver.model.PcAppearance;

/**
 * представление игрока
 * Created by arksu on 02.02.15.
 */
public class PlayerAppearance extends GameServerPacket
{
	PcAppearance _appearance;

	public PlayerAppearance(PcAppearance appearance)
	{
		_appearance = appearance;
	}

	@Override
	protected void write()
	{
		writeC(0x13);
		writeD(_appearance.getObjectId());
		writeC(_appearance.isFemale() ? 1 : 0);
		writeC(_appearance.getHairStyle());
		writeC(_appearance.getHairColor());
		writeC(_appearance.getFace());

		// todo: paperdoll
	}
}
