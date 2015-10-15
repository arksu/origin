package com.a2client.network.game.clientpackets;

/**
 * Created by arksu on 18.10.15.
 */
public class Action extends GameClientPacket
{
	private final String _name;

	public Action(String name)
	{
		_name = name;
	}

	@Override
	protected void write()
	{
		writeC(0x21);
		writeS(_name);
	}
}
