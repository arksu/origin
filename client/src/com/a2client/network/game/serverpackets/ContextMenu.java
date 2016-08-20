package com.a2client.network.game.serverpackets;

import com.a2client.network.game.GamePacketHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arksu on 20.08.16.
 */
public class ContextMenu extends GameServerPacket
{
	static
	{
		GamePacketHandler.AddPacketType(0x23, ContextMenu.class);
	}

	private int _objectId;
	private List<String> _list;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		int count = readC();
		_list = new ArrayList<>();
		while (count > 0)
		{
			_list.add(readS());
			count--;
		}
	}

	@Override
	public void run()
	{

	}
}
