package com.a2client.network.game.clientpackets;

public class CharacterDelete extends GameClientPacket
{
	private int _char_id;

	public CharacterDelete(int charid)
	{
		_char_id = charid;
	}

	@Override
	protected void write()
	{
		writeC(0x06);
		writeD(_char_id);
	}
}
