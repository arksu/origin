package com.a2client.network.game.clientpackets;

/**
 * Created by arksu on 20.08.16.
 */
public class ContextSelect extends GameClientPacket
{
	private final String _selected;

	public ContextSelect(String selected)
	{
		_selected = selected;
	}

	@Override
	protected void write()
	{
		writeC(0x24);
		writeS(_selected);
	}
}
