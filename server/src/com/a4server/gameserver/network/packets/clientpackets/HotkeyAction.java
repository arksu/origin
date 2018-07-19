package com.a4server.gameserver.network.packets.clientpackets;

import com.a4server.gameserver.network.packets.serverpackets.InventoryUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by arksu on 26.02.15.
 */
public class HotkeyAction extends GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(HotkeyAction.class.getName());

	private int _hotkey;

	@Override
	public void readImpl()
	{
		_hotkey = readD();
	}

	@Override
	public void run()
	{
		switch (_hotkey)
		{
			case 1:
				sendPacket(new InventoryUpdate(getClient().getPlayer().getInventory()));
				break;
		}
	}
}
