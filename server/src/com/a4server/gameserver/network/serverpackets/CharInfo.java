package com.a4server.gameserver.network.serverpackets;

import com.a4server.gameserver.model.Player;

/**
 * Created by arksu on 04.02.15.
 */
public class CharInfo extends GameServerPacket
{
	private final Player _player;

	public CharInfo(Player player)
	{
		_player = player;
	}

	@Override
	protected void write()
	{
		writeC(0x0D);
		writeD(_player.getObjectId());
		writeS(_player.getName());
	}
}
