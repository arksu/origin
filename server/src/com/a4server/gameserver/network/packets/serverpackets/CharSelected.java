package com.a4server.gameserver.network.packets.serverpackets;

import com.a4server.gameserver.model.Player;

/**
 * Created by arksu on 05.01.2015.
 */
public class CharSelected extends GameServerPacket
{
	private Player _player;

	public CharSelected(Player player)
	{
		_player = player;
	}

	@Override
	protected void write()
	{
		writeC(0x0A);

		writeD(_player.getObjectId());
		writeS(_player.getName());
	}
}
