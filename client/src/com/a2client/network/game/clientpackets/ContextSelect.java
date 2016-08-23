package com.a2client.network.game.clientpackets;

/**
 * Created by arksu on 20.08.16.
 */
public class ContextSelect extends GameClientPacket
{
	private final String _selected;
	private final int _objectId;

	public ContextSelect(int objectId, String selected)
	{
		_selected = selected;
		_objectId = objectId;
	}

	@Override
	protected void write()
	{
		writeC(0x24);
		writeD(_objectId);
		writeS(_selected);
	}
}
